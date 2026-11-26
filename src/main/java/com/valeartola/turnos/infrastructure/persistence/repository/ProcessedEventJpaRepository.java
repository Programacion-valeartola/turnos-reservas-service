package com.valeartola.turnos.infrastructure.persistence.repository;

import com.valeartola.turnos.infrastructure.persistence.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventEntity, String> {
}