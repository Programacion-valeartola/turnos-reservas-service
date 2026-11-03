package com.valeartola.turnos.infrastructure.persistence.repository;

import com.valeartola.turnos.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByLogin(String login);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEntity> findOneByLogin(String login);
}