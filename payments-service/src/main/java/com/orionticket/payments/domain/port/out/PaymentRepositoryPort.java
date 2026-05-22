package com.orionticket.payments.domain.port.out;

import com.orionticket.payments.domain.model.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepositoryPort {

    Payment save(Payment payment);

    Optional<Payment> findById(UUID paymentId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByGatewayReference(String gatewayReference);
}