package com.orionticket.orders.order.infrastructure.adapters.in.rest;

import com.orionticket.orders.order.application.port.in.CreateOrderCommand;
import com.orionticket.orders.order.application.port.in.OrderUseCase;
import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.infrastructure.adapters.in.rest.dto.*;
import com.orionticket.orders.shared.infrastructure.security.AuthenticatedUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    // POST /v1/orders — crea una orden a partir de una reserva activa
    @PostMapping("/orders")
    @PreAuthorize("hasAuthority('orders:create') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderCommand command = CreateOrderCommand.builder()
                .buyerId(authenticatedUserResolver.currentUserId())
                .eventId(request.getEventId())
                .dateId(request.getDateId())
                .reservationId(request.getReservationId())
                .promotionCode(request.getPromotionCode())
                .build();

        Order order = orderUseCase.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    // GET /v1/orders/{orderId} — detalle de una orden
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAuthority('orders:read:self') or hasAuthority('orders:read') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        Order order = orderUseCase.getOrderById(orderId);
        authenticatedUserResolver.requireSelfOrSupport(order.getBuyerId());
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    // GET /v1/buyers/{buyerId}/orders — historial de órdenes de un comprador (paginado)
    @GetMapping("/buyers/{buyerId}/orders")
    @PreAuthorize("hasAuthority('orders:read:self') or hasAuthority('orders:read') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PagedOrdersResponse> getOrdersByBuyer(
            @PathVariable UUID buyerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        authenticatedUserResolver.requireSelfOrSupport(buyerId);
        Page<Order> result = orderUseCase.getOrdersByBuyer(buyerId, PageRequest.of(page, size));

        List<OrderSummaryResponse> summaries = result.getContent().stream()
                .map(OrderSummaryResponse::from)
                .toList();

        return ResponseEntity.ok(PagedOrdersResponse.builder()
                .orders(summaries)
                .page(result.getNumber())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .build());
    }
}
