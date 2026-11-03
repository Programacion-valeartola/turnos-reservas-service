package com.valeartola.turnos.infrastructure.web;

import com.valeartola.turnos.domain.model.AccessToken;
import com.valeartola.turnos.domain.ports.in.AuthenticateUserUseCase;
import com.valeartola.turnos.infrastructure.web.dto.LoginRequest;
import com.valeartola.turnos.infrastructure.web.dto.TokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada REST: inicio de sesión de usuarios finales. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticateUserUseCase authenticateUser;

    @PostMapping("/authenticate")
    public ResponseEntity<TokenResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        AccessToken token = authenticateUser.authenticate(request.username(), request.password());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.value())
                .body(new TokenResponse(token.value()));
    }
}