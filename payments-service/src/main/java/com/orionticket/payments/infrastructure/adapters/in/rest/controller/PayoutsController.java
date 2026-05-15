package com.orionticket.payments.infrastructure.adapters.in.rest.controller;

import com.orionticket.payments.application.port.in.ManagePayoutsUseCase;
import com.orionticket.payments.domain.model.Payout;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.PayoutListResponse;
import com.orionticket.payments.infrastructure.adapters.in.rest.dto.PayoutResponse;
import com.orionticket.payments.infrastructure.adapters.in.rest.mapper.PaymentDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for payout operations.
 * All business logic is delegated to use case ports — no logic in this class.
 */
@RestController
@RequestMapping("/v1/payouts")
@Tag(name = "Payouts", description = "Organizer payout query endpoints")
public class PayoutsController {

    private final ManagePayoutsUseCase managePayouts;
    private final PaymentDtoMapper mapper;

    public PayoutsController(ManagePayoutsUseCase managePayouts, PaymentDtoMapper mapper) {
        this.managePayouts = managePayouts;
        this.mapper = mapper;
    }

    /**
     * GET /v1/payouts
     * List payouts, optionally filtered by organizerId and status. (UC-PA-03)
     */
    @Operation(summary = "List payouts", description = "Lists payouts, optionally filtered by organizer and payout status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payouts returned"),
            @ApiResponse(responseCode = "400", description = "Invalid filter value")
    })
    @GetMapping
    public ResponseEntity<PayoutListResponse> listPayouts(
            @RequestParam(required = false) UUID organizerId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Payout.PayoutStatus payoutStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                payoutStatus = Payout.PayoutStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid status value: " + status +
                        ". Valid values: PENDING, PROCESSED, FAILED");
            }
        }

        List<Payout> payouts = managePayouts.listPayouts(organizerId, payoutStatus, page, size);
        List<PayoutResponse> responses = payouts.stream().map(mapper::toResponse).toList();
        // totalPages is -1 for v1 (in-memory pagination — no count query)
        return ResponseEntity.ok(new PayoutListResponse(responses, page, -1));
    }

    /**
     * GET /v1/payouts/{payoutId}
     * Retrieve a single payout by ID.
     */
    @Operation(summary = "Get payout", description = "Returns a payout by payout ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payout found"),
            @ApiResponse(responseCode = "404", description = "Payout not found")
    })
    @GetMapping("/{payoutId}")
    public ResponseEntity<PayoutResponse> getPayout(@PathVariable UUID payoutId) {
        Payout payout = managePayouts.getPayout(payoutId);
        return ResponseEntity.ok(mapper.toResponse(payout));
    }
}
