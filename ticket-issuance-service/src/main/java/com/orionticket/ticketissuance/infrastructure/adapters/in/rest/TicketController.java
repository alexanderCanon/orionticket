package com.orionticket.ticketissuance.infrastructure.adapters.in.rest;

import com.orionticket.ticketissuance.application.port.in.TicketQueryUseCase;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.BuyerTicketsResponse;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto.TicketResponse;
import com.orionticket.ticketissuance.infrastructure.adapters.in.rest.mapper.TicketRestMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1")
public class TicketController {

    private final TicketQueryUseCase ticketQueryUseCase;
    private final TicketRestMapper ticketRestMapper;

    public TicketController(TicketQueryUseCase ticketQueryUseCase, TicketRestMapper ticketRestMapper) {
        this.ticketQueryUseCase = ticketQueryUseCase;
        this.ticketRestMapper = ticketRestMapper;
    }

    @GetMapping("/tickets/{ticketId}")
    public TicketResponse getTicket(@PathVariable UUID ticketId) {
        return ticketRestMapper.toResponse(ticketQueryUseCase.getTicket(ticketId));
    }

    @GetMapping("/buyers/{buyerId}/tickets")
    public BuyerTicketsResponse listBuyerTickets(
            @PathVariable UUID buyerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return new BuyerTicketsResponse(
                ticketQueryUseCase.listBuyerTickets(buyerId, page, size).stream()
                        .map(ticketRestMapper::toResponse)
                        .toList(),
                page,
                0
        );
    }
}
