package com.orionticket.payments.infrastructure.adapters.out.persistence;

import com.orionticket.payments.domain.model.Payment;
import com.orionticket.payments.domain.port.out.PaymentRepositoryPort;
import com.orionticket.payments.infrastructure.adapters.out.persistence.mapper.PaymentMapper;
import com.orionticket.payments.infrastructure.adapters.out.persistence.repository.SpringDataPaymentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final SpringDataPaymentRepository repository;
    private final PaymentMapper mapper;

    public PaymentRepositoryAdapter(SpringDataPaymentRepository repository, PaymentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Payment save(Payment payment) {
        return mapper.toDomain(repository.save(mapper.toEntity(payment)));
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return repository.findById(paymentId).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).map(mapper::toDomain);
    }

    @Override
    public Optional<Payment> findByGatewayReference(String gatewayReference) {
        return repository.findByGatewayReference(gatewayReference).map(mapper::toDomain);
    }
}