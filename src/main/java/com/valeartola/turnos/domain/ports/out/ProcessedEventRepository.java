package com.valeartola.turnos.domain.ports.out;

/** Puerto de salida: registro de eventos ya procesados (idempotencia por eventId). */
public interface ProcessedEventRepository {

    boolean isProcessed(String eventId);

    void markProcessed(String eventId, String eventType);
}