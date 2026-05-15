package com.orionticket.payments.infrastructure.adapters.in.rest.mapper;

import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.PaymentResponse;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.PayoutResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentDtoMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setServiceFee(payment.getServiceFee());
        response.setCurrency(payment.getCurrency());
        response.setMethod(payment.getMethod().name());
        response.setIdempotencyKey(payment.getIdempotencyKey());
        response.setStatus(payment.getStatus().name());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    public PayoutResponse toResponse(Payout payout) {
        if (payout == null) {
            return null;
        }
        PayoutResponse response = new PayoutResponse();
        response.setPayoutId(payout.getPayoutId());
        response.setOrganizerId(payout.getOrganizerId());
        response.setEventId(payout.getEventId());
        response.setDateId(payout.getDateId());
        response.setGrossAmount(payout.getGrossAmount());
        response.setServiceFeeTotal(payout.getServiceFeeTotal());
        response.setNetAmount(payout.getNetAmount());
        response.setStatus(payout.getStatus().name());
        response.setTriggeredAt(payout.getTriggeredAt());
        response.setProcessedAt(payout.getProcessedAt());
        return response;
    }
}