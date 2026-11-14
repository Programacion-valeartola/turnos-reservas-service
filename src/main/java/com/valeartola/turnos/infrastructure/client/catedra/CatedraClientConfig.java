package com.valeartola.turnos.infrastructure.client.catedra;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Cliente HTTP hacia la API de la cátedra, autenticado con el JWT técnico del proyecto.
 * El token sale del .env y nunca se envía a la app ni se registra en los logs.
 */
@Configuration
public class CatedraClientConfig {

    @Bean
    public RestClient catedraRestClient(@Value("${catedra.api.base-url}") String baseUrl,
                                        @Value("${catedra.api.token}") String token,
                                        @Value("${catedra.api.connect-timeout-ms:5000}") long connectTimeoutMs,
                                        @Value("${catedra.api.read-timeout-ms:10000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .requestFactory(requestFactory)
                .build();
    }
}