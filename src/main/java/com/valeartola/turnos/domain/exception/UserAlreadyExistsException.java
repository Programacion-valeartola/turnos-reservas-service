package com.valeartola.turnos.domain.exception;

/** El login o el email ya pertenecen a otro usuario. */
public class UserAlreadyExistsException extends RuntimeException {

    public static final String LOGIN_ALREADY_USED = "LOGIN_ALREADY_USED";
    public static final String EMAIL_ALREADY_USED = "EMAIL_ALREADY_USED";

    private final String code;

    public UserAlreadyExistsException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}