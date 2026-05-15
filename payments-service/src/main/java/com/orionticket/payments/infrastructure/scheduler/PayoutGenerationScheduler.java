package com.orionticket.payments.infrastructure.scheduler;

import com.orionticket.payments.application.port.out.EventSummaryPort;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort.PayoutEvent;
import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import com.orionticket.payments.domain.port.out.PayoutRepositoryPort;
import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.DateProjectionEntity;
import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.OrderProjectionEntity;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataDateProjectionRepository;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataOrderProjectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Scheduled job that generates Payouts for Event Dates that have passed (ADR-009).
 *
 * Trigger: every hour (configurable via scheduling.payout-generation.cron).
 * Idempotency: guarded by DateProjectionEntity.payoutGenerated flag.
 *
 * Flow per eligible Date:
 *   1. Find all Orders for the dateId.
 *   2. Sum total and serviceFee of AUTHORIZED Payments for those Orders.
 *   3. Fetch organizerId from Event Management service.
 *   4. Create and persist Payout via domain factory.
 *   5. Mark DateProjection as payoutGenerated = true.
 *   6. Publish PayoutGenerated event.
 */
@Component
public class PayoutGenerationScheduler {

    private static final Logger log = LoggerFactory.getLogger(PayoutGenerationScheduler.class);

    private final SpringDataDateProjectionRepository dateProjectionRepository;
    private final SpringDataOrderProjectionRepository orderProjectionRepository;
    private final PaymentRepositoryPort paymentRepository;
    private final PayoutRepositoryPort payoutRepository;
    private final EventSummaryPort eventSummary;
    private final PaymentEventPublisherPort eventPublisher;

    public PayoutGenerationScheduler(
            SpringDataDateProjectionRepository dateProjectionRepository,
            SpringDataOrderProjectionRepository orderProjectionRepository,
            PaymentRepositoryPort paymentRepository,
            PayoutRepositoryPort payoutRepository,
            EventSummaryPort eventSummary,
            PaymentEventPublisherPort eventPublisher) {
        this.dateProjectionRepository = dateProjectionRepository;
        this.orderProjectionRepository = orderProjectionRepository;
        this.paymentRepository = paymentRepository;
        this.payoutRepository = payoutRepository;
        this.eventSummary = eventSummary;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "${scheduling.payout-generation.cron:0 0 * * * *}")
    @Transactional
    public void generatePayoutsForPassedDates() {
        Instant now = Instant.now();
        List<DateProjectionEntity> eligibleDates =
                dateProjectionRepository.findByScheduledAtBeforeAndPayoutGeneratedFalse(now);

        if (eligibleDates.isEmpty()) {
            log.debug("Payout scheduler — no eligible dates found at {}", now);
            return;
        }

        log.info("Payout scheduler — processing {} eligible date(s)", eligibleDates.size());

        for (DateProjectionEntity dateProjection : eligibleDates) {
            try {
                processDate(dateProjection);
            } catch (Exception e) {
                // Log and continue — one failed date must not block others
                log.error("Failed to generate payout for dateId={} — {}",
                        dateProjection.getDateId(), e.getMessage(), e);
            }
        }
    }

    private void processDate(DateProjectionEntity dateProjection) {
        UUID dateId = dateProjection.getDateId();
        UUID eventId = dateProjection.getEventId();
        log.info("Generating payout — dateId={} eventId={}", dateId, eventId);

        // 1. Find all orders for this dateId
        List<OrderProjectionEntity> orders = orderProjectionRepository.findByDateId(dateId);
        if (orders.isEmpty()) {
            log.warn("No orders found for dateId={} — marking done to avoid repeated checks", dateId);
            markDone(dateProjection);
            return;
        }

        // 2. Aggregate totals from AUTHORIZED payments only
        BigDecimal grossAmount = BigDecimal.ZERO;
        BigDecimal serviceFeeTotal = BigDecimal.ZERO;

        for (OrderProjectionEntity order : orders) {
            Optional<com.orionticket.payments.domain.model.Payment> paymentOpt =
                    paymentRepository.findByOrderId(order.getOrderId());

            if (paymentOpt.isPresent()) {
                com.orionticket.payments.domain.model.Payment payment = paymentOpt.get();
                if (payment.getStatus() == com.orionticket.payments.domain.model.Payment.PaymentStatus.AUTHORIZED) {
                    grossAmount = grossAmount.add(payment.getAmount());
                    serviceFeeTotal = serviceFeeTotal.add(payment.getServiceFee());
                }
            }
        }

        if (grossAmount.compareTo(BigDecimal.ZERO) == 0) {
            log.info("No authorized payments for dateId={} — skipping payout generation", dateId);
            markDone(dateProjection);
            return;
        }

        // 3. Fetch organizerId from Event Management
        UUID organizerId = eventSummary.findByEventId(eventId).organizerId();

        // 4. Create Payout via domain factory
        Payout payout = Payout.generate(organizerId, eventId, dateId, grossAmount, serviceFeeTotal);
        Payout saved = payoutRepository.save(payout);
        log.info("Payout created — payoutId={} organizerId={} netAmount={}",
                saved.getPayoutId(), organizerId, saved.getNetAmount());

        // 5. Mark date as done (idempotency guard)
        markDone(dateProjection);

        // 6. Publish PayoutGenerated event
        try {
            eventPublisher.publishPayoutGenerated(new PayoutEvent(
                    saved.getPayoutId().toString(),
                    saved.getOrganizerId().toString(),
                    saved.getEventId().toString(),
                    saved.getDateId().toString(),
                    saved.getGrossAmount(),
                    saved.getServiceFeeTotal(),
                    saved.getNetAmount(),
                    saved.getStatus().name()
            ));
        } catch (Exception e) {
            log.error("Failed to publish PayoutGenerated — payoutId={}", saved.getPayoutId(), e);
        }
    }

    private void markDone(DateProjectionEntity dateProjection) {
        dateProjection.setPayoutGenerated(true);
        dateProjectionRepository.save(dateProjection);
    }
}
