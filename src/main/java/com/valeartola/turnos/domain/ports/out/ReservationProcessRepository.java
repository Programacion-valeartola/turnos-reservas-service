package com.valeartola.turnos.domain.ports.out;

import com.valeartola.turnos.domain.model.ReservationProcess;

import java.util.Optional;

/** Puerto de salida: guardado de los procesos de reserva. */
public interface ReservationProcessRepository {

    /** Crea o actualiza el proceso y lo devuelve con su id asignado. */
    ReservationProcess save(ReservationProcess process);

    /** Busca un proceso solo si pertenece al usuario indicado (propiedad por usuario). */
    Optional<ReservationProcess> findByIdAndUserId(Long id, Long userId);
}