package com.valeartola.turnos.infrastructure.persistence.repository;

import com.valeartola.turnos.infrastructure.persistence.ReservationProcessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationProcessJpaRepository extends JpaRepository<ReservationProcessEntity, Long> {

    Optional<ReservationProcessEntity> findByIdAndUserId(Long id, Long userId);
}