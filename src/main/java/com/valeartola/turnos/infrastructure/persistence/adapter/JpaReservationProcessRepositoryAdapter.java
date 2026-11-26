package com.valeartola.turnos.infrastructure.persistence.adapter;


import com.valeartola.turnos.domain.model.ReservationProcess;
import com.valeartola.turnos.domain.ports.out.ReservationProcessRepository;
import com.valeartola.turnos.infrastructure.persistence.entity.ReservationProcessEntity;
import com.valeartola.turnos.infrastructure.persistence.repository.ReservationProcessJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Adaptador de salida: guarda los procesos de reserva en PostgreSQL. */
@Component
@RequiredArgsConstructor
public class JpaReservationProcessRepositoryAdapter implements ReservationProcessRepository {

    private final ReservationProcessJpaRepository jpaRepository;

    @Override
    public ReservationProcess save(ReservationProcess process) {
        return toDomain(jpaRepository.save(toEntity(process)));
    }

    @Override
    public Optional<ReservationProcess> findByReservationProcessId(String reservationProcessId) {
        return jpaRepository.findByReservationProcessId(reservationProcessId)
                .map(JpaReservationProcessRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<ReservationProcess> findByIdAndUserId(Long id, Long userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(JpaReservationProcessRepositoryAdapter::toDomain);
    }

    private static ReservationProcessEntity toEntity(ReservationProcess process) {
        ReservationProcessEntity entity = new ReservationProcessEntity();
        entity.setId(process.id());
        entity.setUserId(process.userId());
        entity.setProfessionalId(process.professionalId());
        entity.setAppointmentDate(process.appointmentDate());
        entity.setStartTime(process.startTime());
        entity.setStatus(process.status());
        entity.setHoldId(process.holdId());
        entity.setReservationProcessId(process.reservationProcessId());
        entity.setRequestEventId(process.requestEventId());
        entity.setReservationId(process.reservationId());
        entity.setExpiresAt(process.expiresAt());
        entity.setFailureReason(process.failureReason());
        return entity;
    }

    private static ReservationProcess toDomain(ReservationProcessEntity entity) {
        return new ReservationProcess(
                entity.getId(),
                entity.getUserId(),
                entity.getProfessionalId(),
                entity.getAppointmentDate(),
                entity.getStartTime(),
                entity.getStatus(),
                entity.getHoldId(),
                entity.getReservationProcessId(),
                entity.getRequestEventId(),
                entity.getReservationId(),
                entity.getExpiresAt(),
                entity.getFailureReason());
    }
}