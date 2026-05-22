package com.orionticket.ticketissuance.infrastructure.adapters.in.rest;

import com.orionticket.ticketissuance.application.port.in.CancelTicketUseCase;
import com.orionticket.ticketissuance.application.port.in.InvalidateTicketUseCase;
import com.orionticket.ticketissuance.application.port.in.IssueTicketUseCase;
import com.orionticket.ticketissuance.application.port.in.TicketQueryUseCase;
import com.orionticket.ticketissuance.application.port.in.command.CancelTicketCommand;
import com.orionticket.ticketissuance.application.port.in.command.InvalidateTicketCommand;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.BuyerTicketsResponse;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.IssueTicketRequest;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.TicketResponse;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.mapper.TicketRestMapper;
import com.orionticket.ticketissuance.infrastructure.security.AuthenticatedUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1")
@Tag(name = "Tickets", description = "Ticket issuance, lifecycle, and buyer ticket query endpoints")
public class TicketController {

    private final TicketQueryUseCase ticketQueryUseCase;
    private final IssueTicketUseCase issueTicketUseCase;
    private final CancelTicketUseCase cancelTicketUseCase;
    private final InvalidateTicketUseCase invalidateTicketUseCase;
    private final TicketRestMapper ticketRestMapper;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public TicketController(
            TicketQueryUseCase ticketQueryUseCase,
            IssueTicketUseCase issueTicketUseCase,
            CancelTicketUseCase cancelTicketUseCase,
            InvalidateTicketUseCase invalidateTicketUseCase,
            TicketRestMapper ticketRestMapper,
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        this.ticketQueryUseCase = ticketQueryUseCase;
        this.issueTicketUseCase = issueTicketUseCase;
        this.cancelTicketUseCase = cancelTicketUseCase;
        this.invalidateTicketUseCase = invalidateTicketUseCase;
        this.ticketRestMapper = ticketRestMapper;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @Operation(summary = "Issue ticket", description = "Issues a ticket after payment authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket issued"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Ticket already exists or invalid lifecycle transition")
    })
    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public TicketResponse issueTicket(@Valid @RequestBody IssueTicketRequest request) {
        return ticketRestMapper.toResponse(
                issueTicketUseCase.issueTicket(ticketRestMapper.toCommand(request))
        );
    }

    @Operation(summary = "Cancel ticket", description = "Cancels an issued ticket by ticket ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket canceled"),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "409", description = "Ticket cannot be canceled from its current state")
    })
    @PutMapping("/tickets/{ticketId}/cancel")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('PLATFORM_OPERATOR') or hasRole('SUPER_ADMIN')")
    public TicketResponse cancelTicket(@PathVariable UUID ticketId) {
        return ticketRestMapper.toResponse(
                cancelTicketUseCase.cancelTicket(new CancelTicketCommand(ticketId))
        );
    }

    @Operation(summary = "Invalidate ticket", description = "Invalidates a ticket by ticket ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket invalidated"),
            @ApiResponse(responseCode = "404", description = "Ticket not found"),
            @ApiResponse(responseCode = "409", description = "Ticket cannot be invalidated from its current state")
    })
    @PutMapping("/tickets/{ticketId}/invalidate")
    @PreAuthorize("hasRole('PLATFORM_OPERATOR') or hasRole('SUPER_ADMIN')")
    public TicketResponse invalidateTicket(@PathVariable UUID ticketId) {
        return ticketRestMapper.toResponse(
                invalidateTicketUseCase.invalidateTicket(new InvalidateTicketCommand(ticketId))
        );
    }

    @Operation(summary = "Get ticket", description = "Returns ticket details by ticket ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/tickets/{ticketId}")
    @PreAuthorize("hasRole('BUYER') or hasRole('SUPPORT') or hasRole('DOOR_VALIDATOR') or hasRole('VENUE_STAFF') or hasRole('SUPER_ADMIN')")
    public TicketResponse getTicket(@PathVariable UUID ticketId) {
        var ticket = ticketQueryUseCase.getTicket(ticketId);
        authenticatedUserResolver.requireTicketReadAccess(ticket.buyerId());
        return ticketRestMapper.toResponse(ticket);
    }

    @Operation(summary = "List buyer tickets", description = "Returns paginated tickets owned by a buyer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buyer tickets returned"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/buyers/{buyerId}/tickets")
    @PreAuthorize("hasRole('BUYER') or hasRole('SUPPORT') or hasRole('SUPER_ADMIN')")
    public BuyerTicketsResponse listBuyerTickets(
            @PathVariable UUID buyerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        authenticatedUserResolver.requireBuyerSelfOrPrivileged(buyerId);
        return new BuyerTicketsResponse(
                ticketQueryUseCase.listBuyerTickets(buyerId, page, size).stream()
                        .map(ticketRestMapper::toResponse)
                        .toList(),
                page,
                0
        );
    }
}
