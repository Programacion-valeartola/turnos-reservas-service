package com.valeartola.turnos.infrastructure.web;

import com.valeartola.turnos.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores a respuestas problem+json con un "code" estable,
 * igual que el contrato de la cátedra. Nunca devuelve los valores enviados.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Usuario existente");
        problem.setProperty("code", e.getCode());
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Hay datos inválidos o faltantes");
        problem.setTitle("Datos inválidos");
        problem.setProperty("code", "VALIDATION_ERROR");
        // Solo los nombres de los campos, nunca sus valores (podría ser la contraseña)
        problem.setProperty("fields", e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField).distinct().sorted().toList());
        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido");
        problem.setTitle("Solicitud mal formada");
        problem.setProperty("code", "MALFORMED_REQUEST");
        return problem;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problem.setTitle("Credenciales inválidas");
        problem.setProperty("code", InvalidCredentialsException.CODE);
        return problem;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Usuario inexistente");
        problem.setProperty("code", UserNotFoundException.CODE);
        return problem;
    }

    @ExceptionHandler(ProfessionalNotFoundException.class)
    public ProblemDetail handleProfessionalNotFound(ProfessionalNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setProperty("code", ProfessionalNotFoundException.CODE);
        return problem;
    }

    @ExceptionHandler(ProfessionalDisabledException.class)
    public ProblemDetail handleProfessionalDisabled(ProfessionalDisabledException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setProperty("code", ProfessionalDisabledException.CODE);
        return problem;
    }

    @ExceptionHandler(CatalogUnavailableException.class)
    public ProblemDetail handleCatalogUnavailable(CatalogUnavailableException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "El catálogo no está disponible en este momento. Intente más tarde.");
        problem.setProperty("code", CatalogUnavailableException.CODE);
        return problem;
    }

    @ExceptionHandler(CatedraException.class)
    public ProblemDetail handleCatedra(CatedraException e) {
        HttpStatus status = switch (e.getError()) {
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case UNEXPECTED -> HttpStatus.BAD_GATEWAY;
            case SLOT_ALREADY_HELD, SLOT_ALREADY_RESERVED, RESERVATION_PROCESS_ALREADY_CONFIRMED,
                 RESERVATION_NOT_CANCELLABLE -> HttpStatus.CONFLICT;
            case PROFESSIONAL_NOT_FOUND, HOLD_NOT_FOUND, HOLD_EXPIRED, RESERVATION_NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problem.setProperty("code", e.getError().name());
        return problem;
    }
}