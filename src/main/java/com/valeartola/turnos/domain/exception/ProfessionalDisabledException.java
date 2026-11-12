package com.valeartola.turnos.domain.exception;

/** El profesional existe pero está deshabilitado: no se pueden iniciar reservas. */
public class ProfessionalDisabledException extends RuntimeException {

    public static final String CODE = "PROFESSIONAL_DISABLED";

    public ProfessionalDisabledException(Long id) {
        super("El profesional " + id + " está deshabilitado");
    }
}
