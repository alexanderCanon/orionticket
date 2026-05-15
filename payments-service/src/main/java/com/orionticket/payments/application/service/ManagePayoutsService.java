package com.orionticket.payments.application.service;

import com.orionticket.payments.application.port.in.ManagePayoutsUseCase;
import com.orionticket.payments.domain.exception.PayoutNotFoundException;
import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.domain.port.out.PayoutRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for querying Payout data.
 * Use case: UC-PA-03.
 *
 * Payout generation is triggered asynchronously by the DateAdded event consumer
 * (ADR-009). This service only handles read operations.
 */
@Service
public class ManagePayoutsService implements ManagePayoutsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ManagePayoutsService.class);

    private final PayoutRepositoryPort payoutRepository;

    public ManagePayoutsService(PayoutRepositoryPort payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    /**
     * Lists Payouts for an Organizer, optionally filtered by status.
     * Pagination is applied in-memory for v1; replace with Pageable when
     * the repository is updated to support Spring Data pagination.
     *
     * @param organizerId filter by Organizer; required
     * @param status      optional status filter
     * @param page        zero-indexed page number
     * @param size        page size
     * @return a page of Payout records
     */
    @Override
    @Transactional(readOnly = true)
    public List<Payout> listPayouts(UUID organizerId, Payout.PayoutStatus status, int page, int size) {
        log.debug("Listing payouts — organizerId={} status={} page={} size={}", organizerId, status, page, size);

        List<Payout> all = (status != null)
                ? payoutRepository.findByOrganizerIdAndStatus(organizerId, status)
                : payoutRepository.findByOrganizerId(organizerId);

        // In-memory pagination — acceptable for v1 MVP volumes
        int fromIndex = page * size;
        if (fromIndex >= all.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + size, all.size());
        return all.subList(fromIndex, toIndex);
    }

    /**
     * Retrieves a single Payout by its ID.
     *
     * @param payoutId the Payout to retrieve
     * @return the Payout
     * @throws PayoutNotFoundException if no Payout exists with that ID
     */
    @Override
    @Transactional(readOnly = true)
    public Payout getPayout(UUID payoutId) {
        log.debug("Fetching payout — payoutId={}", payoutId);
        return payoutRepository.findById(payoutId)
                .orElseThrow(() -> new PayoutNotFoundException("Payout not found. payoutId=" + payoutId));
    }
}