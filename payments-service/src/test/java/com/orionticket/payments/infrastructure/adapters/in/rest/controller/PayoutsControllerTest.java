package com.orionticket.payments.infrastructure.adapters.in.rest.controller;

import com.orionticket.payments.application.port.in.ManagePayoutsUseCase;
import com.orionticket.payments.domain.exception.PayoutNotFoundException;
import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.infrastructure.adapters.in.rest.GlobalExceptionHandler;
import com.orionticket.payments.infrastructure.adapters.in.rest.mapper.PaymentDtoMapper;
import com.orionticket.payments.infrastructure.security.AuthenticatedUserResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PayoutsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({PaymentDtoMapper.class, GlobalExceptionHandler.class})
class PayoutsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagePayoutsUseCase managePayouts;

    @MockBean
    private AuthenticatedUserResolver authenticatedUserResolver;

    @Test
    void listPayoutsMapsStatusFilterAndReturnsPage() throws Exception {
        UUID organizerId = UUID.randomUUID();
        Payout payout = payout(organizerId);
        when(authenticatedUserResolver.resolvePayoutOrganizerScope(organizerId)).thenReturn(organizerId);
        when(managePayouts.listPayouts(organizerId, Payout.PayoutStatus.PENDING, 0, 20))
                .thenReturn(List.of(payout));

        mockMvc.perform(get("/v1/payouts")
                        .param("organizerId", organizerId.toString())
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.totalPages").value(-1))
                .andExpect(jsonPath("$.payouts[0].payoutId").value(payout.getPayoutId().toString()))
                .andExpect(jsonPath("$.payouts[0].status").value("PENDING"));

        verify(managePayouts).listPayouts(organizerId, Payout.PayoutStatus.PENDING, 0, 20);
    }

    @Test
    void listPayoutsRejectsInvalidStatus() throws Exception {
        mockMvc.perform(get("/v1/payouts")
                        .param("status", "UNKNOWN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_ARGUMENT"));
    }

    @Test
    void getPayoutReturnsPayoutWhenFound() throws Exception {
        Payout payout = payout(UUID.randomUUID());
        when(managePayouts.getPayout(payout.getPayoutId())).thenReturn(payout);

        mockMvc.perform(get("/v1/payouts/{payoutId}", payout.getPayoutId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payoutId").value(payout.getPayoutId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getPayoutReturnsNotFoundWhenMissing() throws Exception {
        UUID payoutId = UUID.randomUUID();
        when(managePayouts.getPayout(payoutId)).thenThrow(new PayoutNotFoundException("missing"));

        mockMvc.perform(get("/v1/payouts/{payoutId}", payoutId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PAYOUT_NOT_FOUND"));
    }

    private static Payout payout(UUID organizerId) {
        return Payout.generate(
                organizerId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("1000.00"),
                new BigDecimal("90.00"));
    }
}
