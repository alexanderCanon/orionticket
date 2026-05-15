package com.orionticket.ticketissuance.infrastructure.adapters.in.rest.dto;

import java.util.List;

public record BuyerTicketsResponse(
        List<TicketResponse> tickets,
        int page,
        int totalPages
) {
}
