-- Proceso de reserva local: sigue la máquina de estados definida en docs/maquina-estados-reservas.md.
-- Un proceso en estado CONFIRMED representa la reserva confirmada (no se duplica en otra tabla).

CREATE TABLE reservation_process (
    id                      BIGSERIAL    PRIMARY KEY,
    user_id                 BIGINT       NOT NULL REFERENCES jhi_user (id),   -- dueño del proceso
    professional_id         BIGINT       NOT NULL,          -- ID del catálogo (sin FK: otro servicio)
    appointment_date        DATE         NOT NULL,
    start_time              TIME         NOT NULL,          -- hora local de atención
    status                  VARCHAR(30)  NOT NULL,
    hold_id                 VARCHAR(100) UNIQUE,            -- asignado por la cátedra al crear el hold
    reservation_process_id  VARCHAR(100) UNIQUE,            -- asignado por la cátedra; clave de los eventos Kafka
    request_event_id        VARCHAR(255),                   -- eventId de AdditionalInformationRequested
    reservation_id          BIGINT,                         -- ID de la reserva confirmada en la cátedra
    expires_at              TIMESTAMPTZ,
    failure_reason          VARCHAR(100),                   -- code o reason informado por la cátedra
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_reservation_process_status CHECK (status IN (
        'CREATED', 'HELD', 'AWAITING_PHONE_REQUEST', 'PHONE_REQUESTED', 'PHONE_SUBMITTED',
        'CONFIRMED', 'CANCELLED', 'EXPIRED', 'INVALID', 'FAILED'))
);

-- "Mis reservas": búsqueda por usuario. Recuperación: búsqueda de procesos no finales.
CREATE INDEX idx_reservation_process_user   ON reservation_process (user_id);
CREATE INDEX idx_reservation_process_status ON reservation_process (status);

-- Idempotencia de los eventos Kafka recibidos (eventId)
CREATE TABLE processed_event (
    event_id      VARCHAR(255) PRIMARY KEY,
    event_type    VARCHAR(100) NOT NULL,
    processed_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);