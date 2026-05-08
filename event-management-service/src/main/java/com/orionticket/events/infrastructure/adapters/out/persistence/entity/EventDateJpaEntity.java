package com.orionticket.events.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_dates")
@Getter
@Setter
public class EventDateJpaEntity {

    @Id
    @Column(name = "date_id")
    private UUID dateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventJpaEntity event;

    @Column(name = "scheduled_at", nullable = false)
    private ZonedDateTime scheduledAt;

    @Column(name = "venue_id", nullable = false)
    private UUID venueId;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}
