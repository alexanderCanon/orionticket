package com.orionticket.payments.application.port.in;

import com.orionticket.payments.domain.model.Payment;

import java.util.UUID;

public interface InitiatePaymentUseCase {

    Payment initiate(UUID orderId, UUID buyerId, Payment.PaymentMethod method, String gatewayToken);
}