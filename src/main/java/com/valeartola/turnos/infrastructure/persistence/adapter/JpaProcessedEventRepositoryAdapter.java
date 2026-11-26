package com.valeartola.turnos.infrastructure.persistence.adapter;

import com.valeartola.turnos.domain.ports.out.ProcessedEventRepository;
import com.valeartola.turnos.infrastructure.persistence.entity.ProcessedEventEntity;
import com.valeartola.turnos.infrastructure.persistence.repository.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adaptador de salida: guarda los eventId procesados en PostgreSQL. */
@Component
@RequiredArgsConstructor
public class JpaProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository jpaRepository;

    @Override
    public boolean isProcessed(String eventId) {
        return jpaRepository.existsById(eventId);
    }

    @Override
    public void markProcessed(String eventId, String eventType) {
        if (jpaRepository.existsById(eventId)) {
            return;
        }
        ProcessedEventEntity entity = new ProcessedEventEntity();
        entity.setEventId(eventId);
        entity.setEventType(eventType);
        jpaRepository.save(entity);
    }
}