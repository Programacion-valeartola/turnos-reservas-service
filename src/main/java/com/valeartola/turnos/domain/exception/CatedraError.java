package com.valeartola.turnos.domain.exception;

/**
 * Errores de la API de la cátedra traducidos a errores propios (matriz de errores del contrato, sección 13).
 * Se decide siempre por el code, nunca por el texto del detalle.
 */
public enum CatedraError {

    SLOT_ALREADY_HELD("El turno está bloqueado por otra persona"),
    SLOT_ALREADY_RESERVED("El turno ya está reservado"),
    INVALID_SLOT("El horario elegido no corresponde a la agenda del profesional"),
    PROFESSIONAL_NOT_FOUND("El profesional no existe"),
    PROFESSIONAL_DISABLED("El profesional no admite reservas"),
    HOLD_NOT_FOUND("El bloqueo del turno no existe"),
    HOLD_EXPIRED("El bloqueo del turno venció"),
    HOLD_OWNER_MISMATCH("El bloqueo pertenece a otra integración"),
    EXTERNAL_PATIENT_MISMATCH("El paciente no coincide con el del bloqueo"),
    RESERVATION_PROCESS_ALREADY_CONFIRMED("La reserva ya fue confirmada"),
    RESERVATION_NOT_FOUND("La reserva no existe"),
    RESERVATION_OWNER_MISMATCH("La reserva pertenece a otra integración"),
    RESERVATION_NOT_CANCELLABLE("La reserva no se puede cancelar en su estado actual"),
    INVALID_DATE_RANGE("El rango de fechas es inválido"),
    VALIDATION_ERROR("La cátedra rechazó los datos enviados"),
    UNAVAILABLE("El servicio de la cátedra no está disponible"),
    UNEXPECTED("Respuesta inesperada del servicio de la cátedra");

    private final String userMessage;

    CatedraError(String userMessage) {
        this.userMessage = userMessage;
    }

    public String userMessage() {
        return userMessage;
    }

    /** Traduce el code de la cátedra; si no se conoce, decide por el status HTTP. */
    public static CatedraError from(String code, int httpStatus) {
        for (CatedraError error : values()) {
            if (error.name().equals(code)) {
                return error;
            }
        }
        return httpStatus >= 500 ? UNAVAILABLE : UNEXPECTED;
    }
}