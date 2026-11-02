-- Usuarios finales de la aplicación KMP.
-- Estructura compatible con el modelo de usuario generado por JHipster (jhi_user / jhi_authority).

CREATE TABLE jhi_user (
    id                  BIGSERIAL    PRIMARY KEY,
    login               VARCHAR(50)  NOT NULL UNIQUE,
    password_hash       VARCHAR(60)  NOT NULL,          -- hash BCrypt, nunca la contraseña
    first_name          VARCHAR(50),
    last_name           VARCHAR(50),
    email               VARCHAR(191) UNIQUE,
    image_url           VARCHAR(256),
    activated           BOOLEAN      NOT NULL,
    lang_key            VARCHAR(10),
    activation_key      VARCHAR(20),
    reset_key           VARCHAR(20),
    reset_date          TIMESTAMPTZ,
    created_by          VARCHAR(50)  NOT NULL,
    created_date        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_modified_by    VARCHAR(50),
    last_modified_date  TIMESTAMPTZ
);

CREATE TABLE jhi_authority (
    name VARCHAR(50) PRIMARY KEY
);

CREATE TABLE jhi_user_authority (
    user_id        BIGINT      NOT NULL REFERENCES jhi_user (id) ON DELETE CASCADE,
    authority_name VARCHAR(50) NOT NULL REFERENCES jhi_authority (name),
    PRIMARY KEY (user_id, authority_name)
);

-- Roles disponibles. ROLE_SERVICE identifica al JWT técnico entre servicios (decisión de la consulta 1).
INSERT INTO jhi_authority (name) VALUES ('ROLE_ADMIN'), ('ROLE_USER'), ('ROLE_SERVICE');