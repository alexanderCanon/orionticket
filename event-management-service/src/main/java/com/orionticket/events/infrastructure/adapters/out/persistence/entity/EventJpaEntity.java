package com.orionticket.events.infrastructure.adapters.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events")
@Getter
@Setter
public class EventJpaEntity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "organizer_id", nullable = false)
    private UUID organizerId;

    @Column(name = "organizer_name")
    private String organizerName;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<EventDateJpaEntity> dates = new ArrayList<>();

    @Column(name = "created_at")
    private ZonedDateTime createdAt;
}
