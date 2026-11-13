package com.valeartola.turnos.domain.exception;

/**
 * No se pudo obtener la información vigente del catálogo (caído, lento o respuesta inesperada).
 * Sin esa información no se inician operaciones que la requieran.
 */
public class CatalogUnavailableException extends RuntimeException {

    public static final String CODE = "CATALOG_UNAVAILABLE";

    public CatalogUnavailableException(String message) {
        super(message);
    }

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}