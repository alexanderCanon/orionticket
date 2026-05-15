package com.orionticket.payments.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.infrastructure.adapters.out.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentMapper {

    public Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Payment(
            entity.getPaymentId(),
            entity.getOrderId(),
            entity.getBuyerId(),
            entity.getAmount(),
            entity.getServiceFee(),
            entity.getCurrency(),
            Payment.PaymentMethod.valueOf(entity.getMethod().name()),
            Payment.PaymentStatus.valueOf(entity.getStatus().name()),
            entity.getGatewayReference(),
            entity.getIdempotencyKey(),
            entity.getCreatedAt()
        );
    }

    public PaymentJpaEntity toEntity(Payment domain) {
        if (domain == null) {
            return null;
        }
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setPaymentId(domain.getPaymentId());
        entity.setOrderId(domain.getOrderId());
        entity.setBuyerId(domain.getBuyerId());
        entity.setAmount(domain.getAmount());
        entity.setServiceFee(domain.getServiceFee());
        entity.setCurrency(domain.getCurrency());
        entity.setMethod(PaymentJpaEntity.PaymentMethod.valueOf(domain.getMethod().name()));
        entity.setStatus(PaymentJpaEntity.PaymentStatus.valueOf(domain.getStatus().name()));
        entity.setGatewayReference(domain.getGatewayReference());
        entity.setIdempotencyKey(domain.getIdempotencyKey());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}