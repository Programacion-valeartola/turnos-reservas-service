package com.valeartola.turnos.infrastructure.web;

import com.valeartola.turnos.domain.exception.InvalidCredentialsException;
import com.valeartola.turnos.domain.exception.UserAlreadyExistsException;
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
}