package com.valeartola.turnos.infrastructure.client.catalog;

import com.valeartola.turnos.domain.exception.CatalogUnavailableException;
import com.valeartola.turnos.domain.exception.ProfessionalNotFoundException;
import com.valeartola.turnos.domain.model.ProfessionalAgenda;
import com.valeartola.turnos.domain.ports.out.CatalogProvider;
import com.valeartola.turnos.infrastructure.client.catalog.dto.CatalogProfessionalResponse;
import com.valeartola.turnos.infrastructure.security.ServiceTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de salida: consulta al servicio de catálogo por REST, autenticado con el JWT de servicio.
 * El token nunca se registra en los logs.
 */
@Slf4j
@Component
public class CatalogRestAdapter implements CatalogProvider {

    private final RestClient restClient;
    private final ServiceTokenProvider serviceTokenProvider;

    public CatalogRestAdapter(@Qualifier("catalogRestClient") RestClient restClient,
                              ServiceTokenProvider serviceTokenProvider) {
        this.restClient = restClient;
        this.serviceTokenProvider = serviceTokenProvider;
    }

    @Override
    public ProfessionalAgenda getProfessional(Long professionalId) {
        try {
            return restClient.get()
                    .uri("/api/professionals/{id}", professionalId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceTokenProvider.getToken())
                    .exchange((request, response) -> {
                        int status = response.getStatusCode().value();
                        if (status == 200) {
                            CatalogProfessionalResponse body = response.bodyTo(CatalogProfessionalResponse.class);
                            if (body == null) {
                                throw new CatalogUnavailableException("El catálogo devolvió una respuesta vacía");
                            }
                            return body.toDomain();
                        }
                        if (status == 404) {
                            throw new ProfessionalNotFoundException(professionalId);
                        }
                        log.warn("El catálogo respondió {} al pedir el profesional {}", status, professionalId);
                        throw new CatalogUnavailableException("El catálogo respondió " + status);
                    });
        } catch (ResourceAccessException e) {
            // Conexión rechazada o timeout: no hay información vigente
            log.warn("Servicio de catálogo no disponible: {}", e.getMessage());
            throw new CatalogUnavailableException("El servicio de catálogo no responde", e);
        }
    }
}