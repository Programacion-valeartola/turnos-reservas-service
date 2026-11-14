package com.valeartola.turnos.infrastructure.client.catedra;

import com.valeartola.turnos.domain.exception.CatedraError;
import com.valeartola.turnos.domain.exception.CatedraException;
import com.valeartola.turnos.domain.model.CancellationResult;
import com.valeartola.turnos.domain.model.Hold;
import com.valeartola.turnos.domain.model.Occupancy;
import com.valeartola.turnos.domain.model.RemoteAppointment;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Prueba el adaptador con la API de la cátedra simulada. */
class CatedraRestAdapterTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 16);

    private final RestClient.Builder builder = RestClient.builder().baseUrl("http://catedra");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final CatedraRestAdapter adapter = new CatedraRestAdapter(builder.build());

    private void respondProblem(String url, HttpStatus status, String code) {
        server.expect(requestTo(url))
                .andRespond(withStatus(status).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .body("{\"status\":%d,\"code\":\"%s\",\"detail\":\"texto interno\"}".formatted(status.value(), code)));
    }

    @Test
    void leeLasOcupacionesDeUnProfesional() {
        server.expect(requestTo("http://catedra/api/appointment-occupancies?professionalId=215&from=2026-11-16&to=2026-11-16"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"professionalId":215,"from":"2026-11-16","to":"2026-11-16","occupancies":[
                          {"date":"2026-11-16","startTime":"09:00:00","status":"CONFIRMED","expiresAt":null},
                          {"date":"2026-11-16","startTime":"09:30:00","status":"HELD","expiresAt":"2026-11-16T12:44:50Z"}]}
                        """, MediaType.APPLICATION_JSON));

        List<Occupancy> occupancies = adapter.findOccupancies(215L, DAY, DAY);

        assertThat(occupancies).extracting(Occupancy::status).containsExactly("CONFIRMED", "HELD");
        assertThat(occupancies.get(1).startTime()).isEqualTo(LocalTime.of(9, 30));
    }

    @Test
    void creaUnHoldEnviandoLaHoraEnFormatoHHmm() {
        server.expect(requestTo("http://catedra/api/appointment-holds"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"professionalId":215,"date":"2026-11-16","startTime":"09:30","externalPatientId":"user-7"}
                        """))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body("""
                        {"holdId":"hold-1","reservationProcessId":"process-1","status":"HELD","expiresAt":"2026-11-16T12:44:50Z"}
                        """));

        Hold hold = adapter.createHold(215L, DAY, LocalTime.of(9, 30), "user-7");

        assertThat(hold.holdId()).isEqualTo("hold-1");
        assertThat(hold.reservationProcessId()).isEqualTo("process-1");
        assertThat(hold.expiresAt()).isNotNull();
    }

    @Test
    void unTurnoYaBloqueadoSeTraducePorSuCode() {
        respondProblem("http://catedra/api/appointment-holds", HttpStatus.CONFLICT, "SLOT_ALREADY_HELD");

        assertThatThrownBy(() -> adapter.createHold(215L, DAY, LocalTime.of(9, 30), "user-7"))
                .isInstanceOf(CatedraException.class)
                .extracting("error").isEqualTo(CatedraError.SLOT_ALREADY_HELD);
    }

    @Test
    void unHoldVencidoAlConfirmarSeTraducePorSuCode() {
        respondProblem("http://catedra/api/appointment-holds/hold-1/confirm", HttpStatus.NOT_FOUND, "HOLD_EXPIRED");

        assertThatThrownBy(() -> adapter.confirmHold("hold-1", "user-7", "Ana", "Gómez"))
                .isInstanceOf(CatedraException.class)
                .extracting("error").isEqualTo(CatedraError.HOLD_EXPIRED);
    }

    @Test
    void listaLasReservasDeUnPaciente() {
        server.expect(requestTo("http://catedra/api/appointments?externalPatientId=user-7&page=0&size=100"))
                .andRespond(withSuccess("""
                        [{"reservationId":8251,"reservationProcessId":"process-1","externalPatientId":"user-7",
                          "appointmentDate":"2026-11-16","startTime":"09:30:00","endTime":"10:00:00","professionalId":215,
                          "professionalFirstName":"Ana","professionalLastName":"Gómez","status":"CONFIRMED",
                          "patientPhone":"+5492615551234","cancelledAt":null}]
                        """, MediaType.APPLICATION_JSON));

        List<RemoteAppointment> appointments = adapter.findAppointments("user-7");

        assertThat(appointments).hasSize(1);
        assertThat(appointments.getFirst().status()).isEqualTo("CONFIRMED");
    }

    @Test
    void cancelaUnaReserva() {
        server.expect(requestTo("http://catedra/api/appointments/process-1/cancel"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("""
                        {"reservationId":8251,"reservationProcessId":"process-1","status":"CANCELLED","cancelledAt":"2026-11-16T15:10:00Z"}
                        """, MediaType.APPLICATION_JSON));

        CancellationResult result = adapter.cancelAppointment("process-1", "Cambio de planes");

        assertThat(result.status()).isEqualTo("CANCELLED");
    }

    @Test
    void unErrorDeServidorSinCodeSeInformaComoNoDisponible() {
        server.expect(requestTo("http://catedra/api/appointment-occupancies?professionalId=215&from=2026-11-16&to=2026-11-16"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> adapter.findOccupancies(215L, DAY, DAY))
                .isInstanceOf(CatedraException.class)
                .extracting("error").isEqualTo(CatedraError.UNAVAILABLE);
    }
}