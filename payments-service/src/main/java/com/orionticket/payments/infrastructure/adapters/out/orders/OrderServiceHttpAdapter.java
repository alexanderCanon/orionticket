package com.orionticket.payments.infrastructure.adapters.out.orders;

import com.orionticket.payments.application.port.out.OrderSummaryPort;
import com.orionticket.payments.domain.exception.PaymentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * HTTP adapter for fetching Order data from the Orders service.
 * Implements OrderSummaryPort using Spring's RestClient.
 *
 * This adapter makes a synchronous GET /v1/orders/{orderId} call to the
 * Orders service. It is intended as a pragmatic MVP solution until an
 * OrderCreated event projection is available.
 */
@Component
public class OrderServiceHttpAdapter implements OrderSummaryPort {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceHttpAdapter.class);

    private final RestClient restClient;

    public OrderServiceHttpAdapter(RestClient.Builder restClientBuilder,
                                   @Value("${services.orders.base-url}") String ordersBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(ordersBaseUrl).build();
    }

    @Override
    public OrderSummary findByOrderId(UUID orderId) {
        log.debug("Fetching order summary from Orders service — orderId={}", orderId);
        try {
            OrderResponse response = restClient.get()
                    .uri("/v1/orders/{orderId}", orderId)
                    .retrieve()
                    .body(OrderResponse.class);

            if (response == null) {
                throw new PaymentNotFoundException("Orders service returned empty response for orderId=" + orderId);
            }

            return new OrderSummary(
                    response.orderId(),
                    response.buyerId(),
                    response.total(),
                    response.serviceFee(),
                    response.currency(),
                    response.status()
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new PaymentNotFoundException("Order not found in Orders service. orderId=" + orderId);
            }
            log.error("Orders service returned error — orderId={} status={}", orderId, e.getStatusCode());
            throw new com.orionticket.payments.domain.exception.PaymentGatewayException(
                    "Failed to fetch order from Orders service: " + e.getMessage(), e);
        }
    }

    /**
     * Internal DTO for deserializing the GET /v1/orders/{orderId} response.
     * Only maps the fields Payments needs — not the full Order contract.
     */
    record OrderResponse(
            UUID orderId,
            UUID buyerId,
            BigDecimal total,
            BigDecimal serviceFee,
            String currency,
            String status
    ) {}
}
