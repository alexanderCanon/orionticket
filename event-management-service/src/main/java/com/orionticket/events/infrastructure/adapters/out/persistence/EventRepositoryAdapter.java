package com.orionticket.events.infrastructure.adapters.out.persistence;

import com.orionticket.events.domain.model.Event;
import com.orionticket.events.domain.port.out.EventRepositoryPort;
import com.orionticket.events.infrastructure.adapters.out.persistence.entity.EventJpaEntity;
import com.orionticket.events.infrastructure.adapters.out.persistence.mapper.EventMapper;
import com.orionticket.events.infrastructure.adapters.out.persistence.repository.SpringDataEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventRepositoryAdapter implements EventRepositoryPort {

    private final SpringDataEventRepository repository;

    @Override
    public Event save(Event event) {
        EventJpaEntity entity = EventMapper.toEntity(event);
        EventJpaEntity saved = repository.save(entity);
        return EventMapper.toDomain(saved);
    }

    @Override
    public Optional<Event> findById(UUID eventId) {
        return repository.findById(eventId).map(EventMapper::toDomain);
    }
}
