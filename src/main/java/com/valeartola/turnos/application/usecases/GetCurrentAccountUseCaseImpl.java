package com.valeartola.turnos.application.usecases;

import com.valeartola.turnos.domain.exception.UserNotFoundException;
import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.in.GetCurrentAccountUseCase;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Devuelve los datos del usuario autenticado, buscándolo por el login del token. */
@Component
@RequiredArgsConstructor
public class GetCurrentAccountUseCaseImpl implements GetCurrentAccountUseCase {

    private final UserRepository userRepository;

    @Override
    public User getAccount(String login) {
        return userRepository.findByLogin(login)
                .filter(User::activated)
                .orElseThrow(UserNotFoundException::new);
    }
}