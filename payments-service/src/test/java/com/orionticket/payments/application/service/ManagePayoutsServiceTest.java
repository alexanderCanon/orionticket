package com.orionticket.payments.application.service;

import com.orionticket.payments.domain.exception.PayoutNotFoundException;
import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.domain.port.out.PayoutRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagePayoutsServiceTest {

    @Mock
    private PayoutRepositoryPort payoutRepository;

    @InjectMocks
    private ManagePayoutsService service;

    @Test
    void listPayoutsAppliesStatusFilterAndPagination() {
        UUID organizerId = UUID.randomUUID();
        List<Payout> payouts = List.of(payout(organizerId), payout(organizerId), payout(organizerId));
        when(payoutRepository.findByOrganizerIdAndStatus(organizerId, Payout.PayoutStatus.PENDING))
                .thenReturn(payouts);

        List<Payout> result = service.listPayouts(organizerId, Payout.PayoutStatus.PENDING, 1, 2);

        assertThat(result).containsExactly(payouts.get(2));
        verify(payoutRepository).findByOrganizerIdAndStatus(organizerId, Payout.PayoutStatus.PENDING);
    }

    @Test
    void listPayoutsReturnsEmptyPageWhenRequestedPageIsOutOfRange() {
        UUID organizerId = UUID.randomUUID();
        when(payoutRepository.findByOrganizerId(organizerId)).thenReturn(List.of(payout(organizerId)));

        List<Payout> result = service.listPayouts(organizerId, null, 2, 20);

        assertThat(result).isEmpty();
    }

    @Test
    void getPayoutReturnsExistingPayout() {
        Payout payout = payout(UUID.randomUUID());
        when(payoutRepository.findById(payout.getPayoutId())).thenReturn(Optional.of(payout));

        Payout result = service.getPayout(payout.getPayoutId());

        assertThat(result).isSameAs(payout);
    }

    @Test
    void getPayoutThrowsWhenPayoutDoesNotExist() {
        UUID payoutId = UUID.randomUUID();
        when(payoutRepository.findById(payoutId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPayout(payoutId))
                .isInstanceOf(PayoutNotFoundException.class);
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
