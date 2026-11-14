package com.valeartola.turnos.infrastructure.client.catedra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.valeartola.turnos.domain.model.CancellationResult;
import com.valeartola.turnos.domain.model.Hold;
import com.valeartola.turnos.domain.model.HoldConfirmation;
import com.valeartola.turnos.domain.model.Occupancy;
import com.valeartola.turnos.domain.model.RemoteAppointment;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * JSON de la API de la cátedra (secciones 8 a 13 del contrato).
 * Las respuestas ignoran campos desconocidos, como exige el contrato v1.
 */
public final class CatedraDtos {

    private CatedraDtos() {
    }

    // ---------- Ocupaciones ----------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OccupanciesResponse(List<OccupancyDto> occupancies) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OccupancyDto(LocalDate date, LocalTime startTime, String status, Instant expiresAt) {
        public Occupancy toDomain() {
            return new Occupancy(date, startTime, status, expiresAt);
        }
    }

    // ---------- Hold ----------

    /** date en yyyy-MM-dd y startTime en HH:mm, como muestra el contrato. */
    public record CreateHoldRequest(Long professionalId, String date, String startTime, String externalPatientId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HoldResponse(String holdId, String reservationProcessId, String status, Instant expiresAt) {
        public Hold toDomain() {
            return new Hold(holdId, reservationProcessId, status, expiresAt);
        }
    }

    public record ConfirmHoldRequest(String externalPatientId, String patientFirstName, String patientLastName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ConfirmHoldResponse(String reservationProcessId, String status, Instant expiresAt) {
        public HoldConfirmation toDomain() {
            return new HoldConfirmation(reservationProcessId, status, expiresAt);
        }
    }

    // ---------- Reservas ----------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AppointmentDto(Long reservationId, String reservationProcessId, String externalPatientId,
                                 LocalDate appointmentDate, LocalTime startTime, LocalTime endTime,
                                 Long professionalId, String professionalFirstName, String professionalLastName,
                                 String status, Instant cancelledAt) {
        public RemoteAppointment toDomain() {
            return new RemoteAppointment(reservationId, reservationProcessId, externalPatientId, appointmentDate,
                    startTime, endTime, professionalId, professionalFirstName, professionalLastName, status, cancelledAt);
        }
    }

    public record CancelRequest(String reason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CancelResponse(Long reservationId, String reservationProcessId, String status, Instant cancelledAt) {
        public CancellationResult toDomain() {
            return new CancellationResult(reservationId, reservationProcessId, status, cancelledAt);
        }
    }

    // ---------- Errores (problem+json) ----------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProblemResponse(String code, String detail) {
    }
}