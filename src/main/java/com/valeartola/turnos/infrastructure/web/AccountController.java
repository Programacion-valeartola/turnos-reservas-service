package com.valeartola.turnos.infrastructure.web;

import com.valeartola.turnos.domain.ports.in.GetCurrentAccountUseCase;
import com.valeartola.turnos.domain.ports.in.RegisterUserUseCase;
import com.valeartola.turnos.infrastructure.web.dto.RegisterRequest;
import com.valeartola.turnos.infrastructure.web.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Adaptador de entrada REST: cuentas de usuarios finales. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final RegisterUserUseCase registerUser;
    private final GetCurrentAccountUseCase getCurrentAccount;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return UserResponse.from(registerUser.register(request.toDomain()));
    }

    /**
     * Datos del usuario autenticado. La identidad sale del token ("sub"),
     * nunca de un parámetro enviado por el cliente.
     */
    @GetMapping("/account")
    public UserResponse getAccount(@AuthenticationPrincipal Jwt jwt) {
        return UserResponse.from(getCurrentAccount.getAccount(jwt.getSubject()));
    }
}