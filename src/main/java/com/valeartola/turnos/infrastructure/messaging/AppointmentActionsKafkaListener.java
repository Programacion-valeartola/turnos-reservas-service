package com.valeartola.turnos.infrastructure.messaging;

import com.valeartola.turnos.domain.ports.in.HandleAppointmentEventUseCase;
import com.valeartola.turnos.infrastructure.messaging.dto.AppointmentEventMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adaptador de entrada: escucha el topic de acciones de turnos de la cátedra.
 * Un mensaje malformado se registra y se descarta sin trabar los siguientes.
 * El offset se confirma recién cuando este método termina (ack-mode: record).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentActionsKafkaListener {

    private final HandleAppointmentEventUseCase handleAppointmentEvent;
    private final JsonMapper jsonMapper;

    @KafkaListener(topics = "${turnos.kafka.actions-topic}",
            autoStartup = "${turnos.kafka.listener-enabled:true}")
    public void onMessage(ConsumerRecord<String, String> record) {
        AppointmentEventMessage message;
        try {
            message = jsonMapper.readValue(record.value(), AppointmentEventMessage.class);
        } catch (JacksonException | IllegalArgumentException e) {
            log.warn("Mensaje descartado (partición {}, offset {}): JSON inválido", record.partition(), record.offset());
            return;
        }

        if (message == null || !message.isValid()) {
            log.warn("Mensaje descartado (partición {}, offset {}): evento de turnos incompleto",
                    record.partition(), record.offset());
            return;
        }

        handleAppointmentEvent.handle(message.toDomain());
    }
}