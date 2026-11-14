package com.valeartola.turnos.domain.ports.out;

import com.valeartola.turnos.domain.model.CancellationResult;
import com.valeartola.turnos.domain.model.Hold;
import com.valeartola.turnos.domain.model.HoldConfirmation;
import com.valeartola.turnos.domain.model.Occupancy;
import com.valeartola.turnos.domain.model.RemoteAppointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Puerto de salida: operaciones de reserva en el servicio de la cátedra.
 * Todas lanzan CatedraException con el error ya traducido.
 */
public interface CatedraAppointmentsGateway {

    /** Turnos no disponibles (CONFIRMED y HELD) de un profesional entre dos fechas, inclusive. */
    List<Occupancy> findOccupancies(Long professionalId, LocalDate from, LocalDate to);

    /** Bloquea temporalmente un turno. */
    Hold createHold(Long professionalId, LocalDate date, LocalTime startTime, String externalPatientId);

    /** Confirma inicialmente un hold; la cátedra pasa a pedir el teléfono por Kafka. */
    HoldConfirmation confirmHold(String holdId, String externalPatientId, String patientFirstName, String patientLastName);

    /** Reservas registradas para un paciente (identificador estable del usuario). */
    List<RemoteAppointment> findAppointments(String externalPatientId);

    /** Cancela una reserva confirmada. Cancelar una ya cancelada es idempotente. */
    CancellationResult cancelAppointment(String reservationProcessId, String reason);
}