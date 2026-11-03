package com.valeartola.turnos.infrastructure.persistence.adapter;

import com.valeartola.turnos.domain.model.User;
import com.valeartola.turnos.domain.ports.out.UserRepository;
import com.valeartola.turnos.infrastructure.persistence.entity.UserEntity;
import com.valeartola.turnos.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** Adaptador de salida: usuarios en PostgreSQL mediante JPA. */
@Component
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

    /** Autor de la auditoría cuando el usuario se registra solo. */
    static final String SYSTEM = "system";

    private final UserJpaRepository repository;

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLogin(String login) {
        return repository.existsByLogin(login);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        Instant now = Instant.now();
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setLogin(user.login());
        entity.setPasswordHash(user.passwordHash());
        entity.setFirstName(user.firstName());
        entity.setLastName(user.lastName());
        entity.setEmail(user.email());
        entity.setImageUrl(user.imageUrl());
        entity.setActivated(user.activated());
        entity.setLangKey(user.langKey());
        entity.setAuthorities(new HashSet<>(user.authorities()));
        entity.setCreatedBy(SYSTEM);
        entity.setCreatedDate(user.createdDate() != null ? user.createdDate() : now);
        entity.setLastModifiedBy(SYSTEM);
        entity.setLastModifiedDate(now);
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByLogin(String login) {
        return repository.findOneByLogin(login).map(this::toDomain);
    }

    private User toDomain(UserEntity e) {
        return new User(e.getId(), e.getLogin(), e.getPasswordHash(), e.getFirstName(), e.getLastName(),
                e.getEmail(), e.getImageUrl(), e.isActivated(), e.getLangKey(),
                Set.copyOf(e.getAuthorities()), e.getCreatedDate());
    }
}