package com.orionticket.payments.application.service;

import com.orionticket.payments.application.port.in.ProcessWebhookUseCase;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort.PaymentEvent;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort.PaymentFailedEvent;
import com.orionticket.payments.domain.exception.PaymentNotFoundException;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for processing payment gateway webhook notifications.
 * Use cases: UC-PA-01, UC-PA-02.
 *
 * The gateway calls POST /v1/payments/webhook with the final authorization
 * result for a previously initiated Payment. This service:
 *   - Locates the Payment by ID.
 *   - Applies the domain state transition (authorize or fail).
 *   - Persists the updated state.
 *   - Publishes the corresponding domain event.
 *
 * On PaymentFailed, the Reservation is released and the Buyer notified
 * by downstream consumers of the PaymentFailed event (BR-PA-10).
 */
@Service
public class ProcessWebhookService implements ProcessWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessWebhookService.class);

    private static final String RESULT_AUTHORIZED = "AUTHORIZED";
    private static final String RESULT_FAILED     = "FAILED";

    private final PaymentRepositoryPort paymentRepository;
    private final PaymentEventPublisherPort eventPublisher;

    public ProcessWebhookService(PaymentRepositoryPort paymentRepository,
                                 PaymentEventPublisherPort eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Processes the gateway webhook. Idempotent: if the Payment is already in
     * AUTHORIZED or FAILED status (duplicate webhook delivery), the call is a no-op.
     *
     * @param paymentId        the internal Payment UUID
     * @param gatewayReference the external gateway transaction reference
     * @param result           "AUTHORIZED" or "FAILED"
     * @param failureReason    human-readable failure description; null when AUTHORIZED
     */
    @Override
    @Transactional
    public void processWebhook(UUID paymentId, String gatewayReference, String result, String failureReason) {
        log.info("Processing webhook — paymentId={} result={} gatewayReference={}",
                paymentId, result, gatewayReference);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for webhook processing. paymentId=" + paymentId));

        // Idempotency: webhook may be delivered more than once by the gateway
        if (payment.getStatus() != Payment.PaymentStatus.INITIATED) {
            log.warn("Duplicate webhook received — paymentId={} alreadyIn={} ignoring",
                    paymentId, payment.getStatus());
            return;
        }

        if (RESULT_AUTHORIZED.equalsIgnoreCase(result)) {
            handleAuthorized(payment, gatewayReference);
        } else if (RESULT_FAILED.equalsIgnoreCase(result)) {
            handleFailed(payment, failureReason);
        } else {
            log.error("Unknown webhook result '{}' — paymentId={}", result, paymentId);
            throw new IllegalArgumentException("Unknown webhook result: " + result);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void handleAuthorized(Payment payment, String gatewayReference) {
        payment.authorize(gatewayReference);
        Payment saved = paymentRepository.save(payment);
        log.info("Payment authorized — paymentId={} gatewayReference={}",
                saved.getPaymentId(), saved.getGatewayReference());

        publishPaymentAuthorized(saved);
    }

    private void handleFailed(Payment payment, String failureReason) {
        payment.fail();
        Payment saved = paymentRepository.save(payment);
        log.info("Payment failed — paymentId={} reason={}", saved.getPaymentId(), failureReason);

        // Publishing PaymentFailed triggers Reservation release and Buyer notification (BR-PA-10)
        publishPaymentFailed(saved, failureReason);
    }

    private void publishPaymentAuthorized(Payment payment) {
        try {
            eventPublisher.publishPaymentAuthorized(new PaymentEvent(
                    payment.getPaymentId().toString(),
                    payment.getOrderId().toString(),
                    payment.getBuyerId().toString(),
                    payment.getAmount(),
                    payment.getServiceFee(),
                    payment.getCurrency(),
                    payment.getMethod().name(),
                    payment.getGatewayReference(),
                    payment.getIdempotencyKey(),
                    payment.getStatus().name()
            ));
        } catch (Exception e) {
            log.error("Failed to publish PaymentAuthorized — paymentId={} error={}",
                    payment.getPaymentId(), e.getMessage(), e);
        }
    }

    private void publishPaymentFailed(Payment payment, String failureReason) {
        try {
            eventPublisher.publishPaymentFailed(new PaymentFailedEvent(
                    payment.getPaymentId().toString(),
                    payment.getOrderId().toString(),
                    payment.getBuyerId().toString(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getMethod().name(),
                    failureReason,
                    payment.getStatus().name()
            ));
        } catch (Exception e) {
            log.error("Failed to publish PaymentFailed — paymentId={} error={}",
                    payment.getPaymentId(), e.getMessage(), e);
        }
    }
}