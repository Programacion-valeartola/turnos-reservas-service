package com.valeartola.turnos.infrastructure.client.catalog;

import com.valeartola.turnos.domain.exception.CatalogUnavailableException;
import com.valeartola.turnos.domain.exception.ProfessionalDisabledException;
import com.valeartola.turnos.domain.exception.ProfessionalNotFoundException;
import com.valeartola.turnos.domain.model.ProfessionalAgenda;
import com.valeartola.turnos.infrastructure.security.ServiceTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Prueba el adaptador con un catálogo simulado (no hace falta levantar el servicio). */
class CatalogRestAdapterTest {

    private static final String SECRET =
            Base64.getEncoder().encodeToString("clave-de-prueba-de-32-bytes-ok!!".getBytes());

    private final RestClient.Builder builder = RestClient.builder().baseUrl("http://catalogo");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final CatalogRestAdapter adapter =
            new CatalogRestAdapter(builder.build(), new ServiceTokenProvider(SECRET, 300));

    private static String professionalJson(boolean enabled) {
        return """
                {"id":215,"firstName":"Ana","lastName":"Gómez","fullName":"Ana Gómez","enabled":%s,
                 "category":{"id":101,"name":"Clinica medica"},
                 "schedules":[
                   {"dayOfWeek":"MONDAY","startTime":"09:00:00","endTime":"12:00:00","slotDurationMinutes":30,"enabled":true},
                   {"dayOfWeek":"TUESDAY","startTime":"09:00:00","endTime":"10:00:00","slotDurationMinutes":30,"enabled":false}],
                 "campoNuevo":"x"}
                """.formatted(enabled);
    }

    @Test
    void consultaConElTokenDeServicioYConvierteLaRespuesta() {
        server.expect(requestTo("http://catalogo/api/professionals/215"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Bearer ")))
                .andRespond(withSuccess(professionalJson(true), MediaType.APPLICATION_JSON));

        ProfessionalAgenda agenda = adapter.getProfessional(215L);

        assertThat(agenda.fullName()).isEqualTo("Ana Gómez");
        assertThat(agenda.categoryName()).isEqualTo("Clinica medica");
        assertThat(agenda.schedules()).hasSize(2);
        assertThat(agenda.activeSchedules()).hasSize(1);
        server.verify();
    }

    @Test
    void unProfesionalInexistenteSeTraduceAlErrorDelDominio() {
        server.expect(requestTo("http://catalogo/api/professionals/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                        .body("{\"code\":\"PROFESSIONAL_NOT_FOUND\"}"));

        assertThatThrownBy(() -> adapter.getProfessional(999L))
                .isInstanceOf(ProfessionalNotFoundException.class);
    }

    @Test
    void unProfesionalDeshabilitadoNoAdmiteReservas() {
        server.expect(requestTo("http://catalogo/api/professionals/215"))
                .andRespond(withSuccess(professionalJson(false), MediaType.APPLICATION_JSON));

        ProfessionalAgenda agenda = adapter.getProfessional(215L);

        assertThat(agenda.enabled()).isFalse();
        assertThatThrownBy(agenda::ensureBookable).isInstanceOf(ProfessionalDisabledException.class);
    }

    @Test
    void siElCatalogoFallaSeInformaComoNoDisponible() {
        server.expect(requestTo("http://catalogo/api/professionals/215"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> adapter.getProfessional(215L))
                .isInstanceOf(CatalogUnavailableException.class);
    }
}