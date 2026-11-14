package com.valeartola.turnos.infrastructure.client.catedra;

import com.valeartola.turnos.domain.exception.CatedraError;
import com.valeartola.turnos.domain.exception.CatedraException;
import com.valeartola.turnos.domain.model.CancellationResult;
import com.valeartola.turnos.domain.model.Hold;
import com.valeartola.turnos.domain.model.HoldConfirmation;
import com.valeartola.turnos.domain.model.Occupancy;
import com.valeartola.turnos.domain.model.RemoteAppointment;
import com.valeartola.turnos.domain.ports.out.CatedraAppointmentsGateway;
import com.valeartola.turnos.infrastructure.client.catedra.dto.CatedraDtos.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Adaptador de salida: API REST de la cátedra para el flujo de reservas.
 * Traduce cada error por su status y code (problem+json), nunca por el texto del detalle.
 */
@Slf4j
@Component
public class CatedraRestAdapter implements CatedraAppointmentsGateway {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    /** La cátedra pagina el listado; un usuario tiene pocas reservas, así que alcanza con una página grande. */
    private static final int PAGE_SIZE = 100;

    private final RestClient restClient;

    public CatedraRestAdapter(@Qualifier("catedraRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<Occupancy> findOccupancies(Long professionalId, LocalDate from, LocalDate to) {
        OccupanciesResponse body = call("ocupaciones", () -> restClient.get()
                .uri(uri -> uri.path("/api/appointment-occupancies")
                        .queryParam("professionalId", professionalId)
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build())
                .exchange((request, response) -> read(response, 200, OccupanciesResponse.class, "ocupaciones")));
        return body.occupancies() == null ? List.of()
                : body.occupancies().stream().map(OccupancyDto::toDomain).toList();
    }

    @Override
    public Hold createHold(Long professionalId, LocalDate date, LocalTime startTime, String externalPatientId) {
        CreateHoldRequest requestBody =
                new CreateHoldRequest(professionalId, date.toString(), startTime.format(HH_MM), externalPatientId);
        return call("crear hold", () -> restClient.post()
                .uri("/api/appointment-holds")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange((request, response) -> read(response, 201, HoldResponse.class, "crear hold")))
                .toDomain();
    }

    @Override
    public HoldConfirmation confirmHold(String holdId, String externalPatientId,
                                        String patientFirstName, String patientLastName) {
        ConfirmHoldRequest requestBody = new ConfirmHoldRequest(externalPatientId, patientFirstName, patientLastName);
        return call("confirmar hold", () -> restClient.post()
                .uri("/api/appointment-holds/{holdId}/confirm", holdId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .exchange((request, response) -> read(response, 202, ConfirmHoldResponse.class, "confirmar hold")))
                .toDomain();
    }

    @Override
    public List<RemoteAppointment> findAppointments(String externalPatientId) {
        AppointmentDto[] body = call("listar reservas", () -> restClient.get()
                .uri(uri -> uri.path("/api/appointments")
                        .queryParam("externalPatientId", externalPatientId)
                        .queryParam("page", 0)
                        .queryParam("size", PAGE_SIZE)
                        .build())
                .exchange((request, response) -> read(response, 200, AppointmentDto[].class, "listar reservas")));
        return Arrays.stream(body).map(AppointmentDto::toDomain).toList();
    }

    @Override
    public CancellationResult cancelAppointment(String reservationProcessId, String reason) {
        return call("cancelar reserva", () -> restClient.post()
                .uri("/api/appointments/{id}/cancel", reservationProcessId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CancelRequest(reason))
                .exchange((request, response) -> read(response, 200, CancelResponse.class, "cancelar reserva")))
                .toDomain();
    }

    // ---------- Manejo común ----------

    /** Ejecuta la llamada; si no hay respuesta (conexión rechazada o timeout), la cátedra no está disponible. */
    private <T> T call(String operation, Supplier<T> request) {
        try {
            return request.get();
        } catch (ResourceAccessException e) {
            log.warn("La cátedra no responde ({}): {}", operation, e.getMessage());
            throw new CatedraException(CatedraError.UNAVAILABLE, 0, e);
        }
    }

    /** Devuelve el cuerpo si el status es el esperado; si no, traduce el error. */
    private <T> T read(ConvertibleClientHttpResponse response, int expectedStatus, Class<T> type, String operation)
            throws IOException {
        int status = response.getStatusCode().value();
        if (status == expectedStatus) {
            T body = response.bodyTo(type);
            if (body == null) {
                throw new CatedraException(CatedraError.UNEXPECTED, status);
            }
            return body;
        }
        throw toException(response, status, operation);
    }

    private CatedraException toException(ConvertibleClientHttpResponse response, int status, String operation) {
        String code = "HTTP_" + status;
        try {
            ProblemResponse problem = response.bodyTo(ProblemResponse.class);
            if (problem != null && problem.code() != null) {
                code = problem.code();
            }
        } catch (RuntimeException ignored) {
            // Sin cuerpo problem+json: se decide solo por el status
        }
        CatedraError error = CatedraError.from(code, status);
        log.warn("La cátedra respondió {} {} al {}", status, code, operation);
        return new CatedraException(error, status);
    }
}