package com.orionticket.events.infrastructure.adapters.in.rest;

import com.orionticket.events.application.port.in.GetEventCatalogUseCase;
import com.orionticket.events.domain.model.Event;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.CatalogResponse;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.EventDateResponse;
import com.orionticket.events.infrastructure.adapters.in.rest.dto.EventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/catalog")
@RequiredArgsConstructor
@Tag(name = "Catalog", description = "Public event catalog endpoints")
public class EventCatalogController {

    private final GetEventCatalogUseCase getEventCatalogUseCase;

    @Operation(summary = "Search public event catalog", description = "Returns released catalog events filtered by category, city, date, or organizer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Catalog events returned"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or pagination parameter")
    })
    @GetMapping("/events")
    public ResponseEntity<CatalogResponse<EventResponse>> getCatalog(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) UUID organizerId,
            Pageable pageable) {
        
        Page<Event> page = getEventCatalogUseCase.getCatalog(category, city, date, organizerId, pageable);
        
        List<EventResponse> eventResponses = page.getContent().stream()
                .map(event -> EventResponse.builder()
                        .eventId(event.getEventId())
                        .name(event.getName())
                        .category(event.getCategory())
                        .organizerName(event.getOrganizerName())
                        .dates(event.getDates().stream()
                                .map(d -> EventDateResponse.builder()
                                        .dateId(d.getDateId())
                                        .scheduledAt(d.getScheduledAt())
                                        .venueName(d.getVenueName())
                                        .availableSeats(d.getAvailableSeats())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
        
        CatalogResponse<EventResponse> response = CatalogResponse.<EventResponse>builder()
                .events(eventResponses)
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .build();
        
        return ResponseEntity.ok(response);
    }
}
