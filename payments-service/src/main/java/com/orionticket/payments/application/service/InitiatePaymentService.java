package com.orionticket.payments.application.service;

import com.orionticket.payments.application.port.in.InitiatePaymentUseCase;
import com.orionticket.payments.application.port.out.OrderSummaryPort;
import com.orionticket.payments.application.port.out.OrderSummaryPort.OrderSummary;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort.PaymentEvent;
import com.orionticket.payments.application.port.out.PaymentEventPublisherPort.PaymentFailedEvent;
import com.orionticket.payments.application.port.out.PaymentGatewayPort;
import com.orionticket.payments.application.port.out.PaymentGatewayPort.GatewayRequest;
import com.orionticket.payments.application.port.out.PaymentGatewayPort.GatewayResponse;
import com.orionticket.payments.domain.exception.PaymentGatewayException;
import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Application service for initiating a payment for an Order.
 * Use case: UC-PA-01.
 *
 * Flow:
 * 1. Check idempotency — if a Payment already exists for this orderId, return it (BR-PA-09).
 * 2. Fetch Order financial data from Orders service.
 * 3. Validate the Order is in a payable state.
 * 4. Create the Payment aggregate via factory method.
 * 5. Submit to the payment gateway.
 * 6. Persist and publish PaymentInitiated.
 */
@Service
public class InitiatePaymentService implements InitiatePaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(InitiatePaymentService.class);

    private final PaymentRepositoryPort paymentRepository;
    private final OrderSummaryPort orderSummary;
    private final PaymentGatewayPort gateway;
    private final PaymentEventPublisherPort eventPublisher;

    public InitiatePaymentService(PaymentRepositoryPort paymentRepository,
                                  OrderSummaryPort orderSummary,
                                  PaymentGatewayPort gateway,
                                  PaymentEventPublisherPort eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.orderSummary = orderSummary;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Initiates a payment for an Order.
     * Idempotent: returns the existing Payment if one already exists for the orderId (BR-PA-09).
     *
     * @param orderId      the Order to pay for
     * @param buyerId      the Buyer initiating the payment
     * @param method       CARD or TRANSFER (BR-PA-02)
     * @param gatewayToken tokenized card or transfer reference — never raw card data (BR-PA-08)
     * @return the Payment in INITIATED status
     */
    @Override
    @Transactional
    public Payment initiate(UUID orderId, UUID buyerId, Payment.PaymentMethod method, String gatewayToken) {
        log.info("Initiating payment — orderId={} buyerId={} method={}", orderId, buyerId, method);

        // 1. Idempotency check: one Payment per Order (BR-PA-09)
        Optional<Payment> existing = paymentRepository.findByOrderId(orderId);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            log.info("Idempotent return — payment already exists paymentId={} status={}",
                    payment.getPaymentId(), payment.getStatus());
            return payment;
        }

        // 2. Fetch Order financial data from Orders service
        OrderSummary order = orderSummary.findByOrderId(orderId);

        // 3. Validate the Order is in a payable state
        if (!"CREATED".equals(order.status())) {
            throw new IllegalStateException(
                    "Order is not in a payable state. orderId=" + orderId + " status=" + order.status());
        }

        // 4. Build idempotency key — orderId is the stable anchor for one payment per order
        String idempotencyKey = "pay-" + orderId;

        // 5. Create the Payment aggregate in INITIATED state via domain factory
        Payment payment = Payment.initiate(
                orderId,
                buyerId,
                order.total(),
                order.serviceFee(),
                order.currency(),
                method,
                idempotencyKey
        );

        // 6. Submit to the payment gateway
        GatewayRequest gatewayRequest = new GatewayRequest(
                idempotencyKey,
                method.name(),
                gatewayToken,
                order.total(),
                order.currency(),
                payment.getPaymentId(),
                payment.getOrderId()
        );

        GatewayResponse gatewayResponse;
        try {
            gatewayResponse = gateway.process(gatewayRequest);
        } catch (Exception e) {
            log.error("Payment gateway submission failed — orderId={} error={}", orderId, e.getMessage());
            throw new PaymentGatewayException("Payment gateway is unavailable: " + e.getMessage(), e);
        }

        if (!gatewayResponse.success()) {
            log.warn("Gateway rejected payment submission — orderId={} reason={}",
                    orderId, gatewayResponse.failureReason());
            // Mark as FAILED immediately if the gateway rejects the submission outright
            payment.fail();
            Payment saved = paymentRepository.save(payment);
            publishPaymentFailed(saved, gatewayResponse.failureReason());
            return saved;
        }

        // Assign external gateway reference
        payment.setGatewayReference(gatewayResponse.gatewayReference());

        // 7. Persist and publish PaymentInitiated
        Payment saved = paymentRepository.save(payment);
        log.info("Payment persisted — paymentId={} status={}", saved.getPaymentId(), saved.getStatus());

        publishPaymentInitiated(saved);
        return saved;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void publishPaymentInitiated(Payment payment) {
        try {
            eventPublisher.publishPaymentInitiated(new PaymentEvent(
                    payment.getPaymentId().toString(),
                    payment.getOrderId().toString(),
                    payment.getBuyerId().toString(),
                    payment.getAmount(),
                    payment.getServiceFee(),
                    payment.getCurrency(),
                    payment.getMethod().name(),
                    null,  // gatewayReference not set at INITIATED stage
                    payment.getIdempotencyKey(),
                    payment.getStatus().name()
            ));
        } catch (Exception e) {
            // Log and continue — payment is already persisted; event can be retried via outbox pattern
            log.error("Failed to publish PaymentInitiated — paymentId={} error={}",
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