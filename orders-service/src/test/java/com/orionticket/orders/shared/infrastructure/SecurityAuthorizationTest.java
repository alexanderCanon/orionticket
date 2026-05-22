package com.orionticket.orders.shared.infrastructure;

import com.orionticket.orders.order.application.port.in.CreateOrderCommand;
import com.orionticket.orders.order.application.port.in.OrderUseCase;
import com.orionticket.orders.order.domain.model.Order;
import com.orionticket.orders.order.domain.model.OrderStatus;
import com.orionticket.orders.order.infrastructure.adapters.in.rest.OrderController;
import com.orionticket.orders.promotion.application.port.in.PromotionUseCase;
import com.orionticket.orders.promotion.domain.model.DiscountType;
import com.orionticket.orders.promotion.domain.model.Promotion;
import com.orionticket.orders.promotion.domain.model.PromotionStatus;
import com.orionticket.orders.promotion.infrastructure.adapters.in.rest.PromotionController;
import com.orionticket.orders.shared.infrastructure.config.SecurityConfig;
import com.orionticket.orders.shared.infrastructure.security.AuthenticatedUserResolver;
import com.orionticket.orders.shared.infrastructure.security.JwtAuthoritiesConverter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        OrderController.class,
        PromotionController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthoritiesConverter.class,
        AuthenticatedUserResolver.class
})
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://identity-service:8081/.well-known/jwks.json",
        "jwt.issuer=orionticket-identity"
})
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderUseCase orderUseCase;

    @MockBean
    private PromotionUseCase promotionUseCase;

    @Test
    void createOrderWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderRequest(UUID.randomUUID())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buyerCreatesOrderWithBuyerIdFromToken() throws Exception {
        UUID buyerId = UUID.randomUUID();
        UUID forgedBuyerId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID dateId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        when(orderUseCase.createOrder(any(CreateOrderCommand.class)))
                .thenReturn(order(buyerId, eventId, dateId, reservationId));

        mockMvc.perform(post("/v1/orders")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(buyerId.toString())
                                        .claim("role", "BUYER")
                                        .claim("permissions", List.of("orders:create")))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_BUYER"),
                                        new SimpleGrantedAuthority("orders:create")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "buyerId": "%s",
                                  "eventId": "%s",
                                  "dateId": "%s",
                                  "reservationId": "%s",
                                  "promotionCode": "EARLY"
                                }
                                """.formatted(forgedBuyerId, eventId, dateId, reservationId)))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateOrderCommand> captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(orderUseCase).createOrder(captor.capture());
        assertThat(captor.getValue().getBuyerId()).isEqualTo(buyerId);
    }

    @Test
    void buyerCannotListAnotherBuyerOrders() throws Exception {
        UUID tokenBuyerId = UUID.randomUUID();
        UUID requestedBuyerId = UUID.randomUUID();

        mockMvc.perform(get("/v1/buyers/" + requestedBuyerId + "/orders")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(tokenBuyerId.toString())
                                        .claim("role", "BUYER")
                                        .claim("permissions", List.of("orders:read:self")))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_BUYER"),
                                        new SimpleGrantedAuthority("orders:read:self"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void supportCanListBuyerOrders() throws Exception {
        UUID buyerId = UUID.randomUUID();
        when(orderUseCase.getOrdersByBuyer(eq(buyerId), eq(PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/v1/buyers/" + buyerId + "/orders")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "SUPPORT")
                                        .claim("permissions", List.of("orders:read")))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_SUPPORT"),
                                        new SimpleGrantedAuthority("orders:read"))))
                .andExpect(status().isOk());
    }

    @Test
    void buyerCannotCreatePromotion() throws Exception {
        mockMvc.perform(post("/v1/promotions")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "BUYER"))
                                .authorities(new SimpleGrantedAuthority("ROLE_BUYER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPromotionRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    void marketingCanCreatePromotion() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(promotionUseCase.createPromotion(
                eq(eventId),
                eq("EARLY"),
                eq(DiscountType.PERCENTAGE),
                eq(BigDecimal.valueOf(15)),
                eq(100)))
                .thenReturn(promotion(eventId));

        mockMvc.perform(post("/v1/promotions")
                        .with(jwt().jwt(jwt -> jwt
                                        .subject(UUID.randomUUID().toString())
                                        .claim("role", "MARKETING")
                                        .claim("permissions", List.of("promotions:manage")))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_MARKETING"),
                                        new SimpleGrantedAuthority("promotions:manage")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "code": "EARLY",
                                  "discountType": "PERCENTAGE",
                                  "discountValue": 15,
                                  "maxUses": 100
                                }
                                """.formatted(eventId)))
                .andExpect(status().isCreated());
    }

    private static String createOrderRequest(UUID buyerId) {
        return """
                {
                  "buyerId": "%s",
                  "eventId": "%s",
                  "dateId": "%s",
                  "reservationId": "%s"
                }
                """.formatted(buyerId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    private static String createPromotionRequest() {
        return """
                {
                  "eventId": "%s",
                  "code": "EARLY",
                  "discountType": "PERCENTAGE",
                  "discountValue": 15,
                  "maxUses": 100
                }
                """.formatted(UUID.randomUUID());
    }

    private static Order order(UUID buyerId, UUID eventId, UUID dateId, UUID reservationId) {
        return Order.builder()
                .orderId(UUID.randomUUID())
                .buyerId(buyerId)
                .eventId(eventId)
                .dateId(dateId)
                .reservationId(reservationId)
                .lineItems(List.of())
                .subtotal(BigDecimal.ZERO)
                .promotionDiscount(BigDecimal.ZERO)
                .serviceFee(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .currency("GTQ")
                .status(OrderStatus.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private static Promotion promotion(UUID eventId) {
        return Promotion.builder()
                .promotionId(UUID.randomUUID())
                .eventId(eventId)
                .code("EARLY")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(15))
                .maxUses(100)
                .usedCount(0)
                .status(PromotionStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();
    }
}
