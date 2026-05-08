package com.orionticket.events.infrastructure.adapters.out.persistence.mapper;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.model.EventDate;
import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventDateJpaEntity;
import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventJpaEntity;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class EventMapper {

    public static Event toDomain(EventJpaEntity entity) {
        if (entity == null) return null;
        
        Event event = Event.builder()
                .eventId(entity.getEventId())
                .organizerId(entity.getOrganizerId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .dates(new ArrayList<>())
                .build();
                
        if (entity.getDates() != null) {
            event.setDates(entity.getDates().stream().map(d -> toDomainDate(d)).collect(Collectors.toList()));
        }
        
        return event;
    }

    public static EventDate toDomainDate(EventDateJpaEntity entity) {
        if (entity == null) return null;
        return EventDate.builder()
                .dateId(entity.getDateId())
                .eventId(entity.getEvent().getEventId())
                .scheduledAt(entity.getScheduledAt())
                .venueId(entity.getVenueId())
                .capacity(entity.getCapacity())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static EventJpaEntity toEntity(Event event) {
        if (event == null) return null;
        
        EventJpaEntity entity = new EventJpaEntity();
        entity.setEventId(event.getEventId());
        entity.setOrganizerId(event.getOrganizerId());
        entity.setName(event.getName());
        entity.setDescription(event.getDescription());
        entity.setStatus(event.getStatus());
        entity.setCreatedAt(event.getCreatedAt());
        
        if (event.getDates() != null) {
            entity.setDates(event.getDates().stream().map(d -> {
                EventDateJpaEntity dateEntity = toEntityDate(d);
                dateEntity.setEvent(entity);
                return dateEntity;
            }).collect(Collectors.toList()));
        }
        
        return entity;
    }

    public static EventDateJpaEntity toEntityDate(EventDate date) {
        if (date == null) return null;
        EventDateJpaEntity entity = new EventDateJpaEntity();
        entity.setDateId(date.getDateId());
        entity.setScheduledAt(date.getScheduledAt());
        entity.setVenueId(date.getVenueId());
        entity.setCapacity(date.getCapacity());
        entity.setCreatedAt(date.getCreatedAt());
        return entity;
    }
}
