package com.valeartola.turnos.infrastructure.web;

import com.valeartola.turnos.domain.ports.in.StartReservationUseCase;
import com.valeartola.turnos.infrastructure.web.dto.ReservationResponse;
import com.valeartola.turnos.infrastructure.web.dto.StartReservationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST: procesos de reserva del usuario autenticado. */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final StartReservationUseCase startReservation;

    /** El usuario sale del token ("sub"); el body solo trae el turno elegido. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse start(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody StartReservationRequest request) {
        return ReservationResponse.from(startReservation.start(
                jwt.getSubject(), request.professionalId(), request.date(), request.startTime()));
    }
}