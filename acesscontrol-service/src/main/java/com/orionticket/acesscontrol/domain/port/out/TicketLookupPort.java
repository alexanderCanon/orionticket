package com.orionticket.acesscontrol.domain.port.out;

import com.orionticket.acesscontrol.domain.model.ValidationRecord;
import java.util.Optional;
import java.util.UUID;

public interface TicketLookupPort {
    Optional<TicketLookupResult> findTicketById(UUID ticketId);
}