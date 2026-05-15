package com.orionticket.acesscontrol.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface TicketLookupPort {
    Optional<TicketLookupResult> findTicketById(UUID ticketId);
}