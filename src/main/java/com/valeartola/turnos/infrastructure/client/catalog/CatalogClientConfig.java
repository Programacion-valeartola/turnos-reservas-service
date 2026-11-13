package com.valeartola.turnos.infrastructure.client.catalog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** Cliente HTTP hacia el servicio de catálogo, con timeouts para no quedar esperando indefinidamente. */
@Configuration
public class CatalogClientConfig {

    @Bean
    public RestClient catalogRestClient(@Value("${catalogo.api.base-url}") String baseUrl,
                                        @Value("${catalogo.api.connect-timeout-ms:3000}") long connectTimeoutMs,
                                        @Value("${catalogo.api.read-timeout-ms:5000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}