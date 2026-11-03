package com.valeartola.turnos.domain.exception;

/** El usuario identificado por el token ya no existe o está desactivado. */
public class UserNotFoundException extends RuntimeException {

    public static final String CODE = "USER_NOT_FOUND";

    public UserNotFoundException() {
        super("El usuario no existe");
    }
}