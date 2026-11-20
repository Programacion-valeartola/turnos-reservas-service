package com.valeartola.turnos.infrastructure.persistence;

import com.valeartola.turnos.domain.model.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reservation_process")
@Getter
@Setter
@NoArgsConstructor
public class ReservationProcessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "professional_id", nullable = false)
    private Long professionalId;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReservationStatus status;

    @Column(name = "hold_id")
    private String holdId;

    @Column(name = "reservation_process_id")
    private String reservationProcessId;

    @Column(name = "request_event_id")
    private String requestEventId;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "failure_reason")
    private String failureReason;

    /** Lo completa la base (DEFAULT now()); Java nunca lo escribe. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    /** Hibernate lo actualiza en cada guardado. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}