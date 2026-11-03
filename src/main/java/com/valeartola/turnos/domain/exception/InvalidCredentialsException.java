package com.valeartola.turnos.domain.exception;

/**
 * Usuario o contraseña incorrectos. El mensaje es siempre el mismo,
 * para no revelar cuál de los dos datos falló.
 */
public class InvalidCredentialsException extends RuntimeException {

    public static final String CODE = "INVALID_CREDENTIALS";

    public InvalidCredentialsException() {
        super("Usuario o contraseña incorrectos");
    }
}