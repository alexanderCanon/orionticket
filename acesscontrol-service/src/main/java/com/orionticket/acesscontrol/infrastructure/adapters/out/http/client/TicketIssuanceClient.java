package com.orionticket.acesscontrol.infrastructure.adapters.out.http.client;

import com.orionticket.acesscontrol.domain.port.out.TicketLookupPort;
import com.orionticket.acesscontrol.domain.port.out.TicketLookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Optional;
import java.util.UUID;

@Component
public class TicketIssuanceClient implements TicketLookupPort {

    private static final Logger log = LoggerFactory.getLogger(TicketIssuanceClient.class);

    @SuppressWarnings("unused")
    private final WebClient webClient;

    public TicketIssuanceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://ticket-issuance-service")
                .build();
    }

    @Override
    public Optional<TicketLookupResult> findTicketById(UUID ticketId) {
        log.info("Looking up ticket: {}", ticketId);
        return Optional.empty();
    }
}