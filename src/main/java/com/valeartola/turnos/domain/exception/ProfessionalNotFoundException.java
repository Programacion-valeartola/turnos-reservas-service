package com.valeartola.turnos.domain.exception;

/** El profesional no existe en el catálogo. */
public class ProfessionalNotFoundException extends RuntimeException {

    public static final String CODE = "PROFESSIONAL_NOT_FOUND";

    public ProfessionalNotFoundException(Long id) {
        super("No existe el profesional " + id);
    }
}