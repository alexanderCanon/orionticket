package com.orionticket.events.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Getter
@Setter
public class VenueJpaEntity {

    @Id
    @Column(name = "venue_id")
    private UUID venueId;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}
