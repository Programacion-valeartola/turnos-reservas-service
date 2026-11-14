package com.valeartola.turnos.domain.exception;

/** Falla de una operación con la cátedra, ya traducida a un CatedraError. */
public class CatedraException extends RuntimeException {

    private final CatedraError error;
    private final int httpStatus;

    public CatedraException(CatedraError error, int httpStatus) {
        super(error.userMessage());
        this.error = error;
        this.httpStatus = httpStatus;
    }

    public CatedraException(CatedraError error, int httpStatus, Throwable cause) {
        super(error.userMessage(), cause);
        this.error = error;
        this.httpStatus = httpStatus;
    }

    public CatedraError getError() {
        return error;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}