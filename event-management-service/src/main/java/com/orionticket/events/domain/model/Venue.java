package com.orionticket.events.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class Venue {
    private UUID venueId;
    private UUID organizerId;
    private String name;
    private String address;
    private String city;
    private Integer capacity;
    private ZonedDateTime createdAt;

    /**
     * Regla de Negocio: Crear un Venue asociado a un Organizador.
     */
    public static Venue create(UUID organizerId, String name, String address, Integer capacity) {
        if (capacity == null || capacity <= 0) {
            throw new IllegalArgumentException("Venue capacity must be greater than 0");
        }
        
        return Venue.builder()
                .venueId(UUID.randomUUID())
                .organizerId(organizerId)
                .name(name)
                .address(address)
                .city("Unknown") // Default or provide it in create method
                .capacity(capacity)
                .createdAt(ZonedDateTime.now())
                .build();
    }
}
