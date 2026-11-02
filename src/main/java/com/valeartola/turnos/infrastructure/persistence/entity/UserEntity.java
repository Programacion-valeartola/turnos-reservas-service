package com.valeartola.turnos.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/** Tabla jhi_user (migración V1). Los roles se leen de jhi_user_authority. */
@Entity
@Table(name = "jhi_user")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email", unique = true, length = 191)
    private String email;

    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Column(name = "activated", nullable = false)
    private boolean activated;

    @Column(name = "lang_key", length = 10)
    private String langKey;

    @Column(name = "activation_key", length = 20)
    private String activationKey;

    @Column(name = "reset_key", length = 20)
    private String resetKey;

    @Column(name = "reset_date")
    private Instant resetDate;

    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private Instant createdDate;

    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "jhi_user_authority", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "authority_name", length = 50)
    private Set<String> authorities = new HashSet<>();
}