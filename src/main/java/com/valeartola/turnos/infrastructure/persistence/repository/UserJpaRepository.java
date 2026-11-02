package com.valeartola.turnos.infrastructure.persistence.repository;

import com.valeartola.turnos.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    boolean existsByLogin(String login);

    boolean existsByEmailIgnoreCase(String email);
}