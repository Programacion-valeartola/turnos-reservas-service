package com.valeartola.turnos.infrastructure.web;

import com.valeartola.turnos.domain.ports.in.RegisterUserUseCase;
import com.valeartola.turnos.infrastructure.web.dto.RegisterRequest;
import com.valeartola.turnos.infrastructure.web.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Adaptador de entrada REST: cuentas de usuarios finales. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final RegisterUserUseCase registerUser;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return UserResponse.from(registerUser.register(request.toDomain()));
    }
}
