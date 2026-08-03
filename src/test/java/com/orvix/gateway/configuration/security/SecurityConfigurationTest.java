package com.orvix.gateway.configuration.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityConfigurationTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void actuatorHealthEndpointShouldBeAccessibleNoAuth() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void actuatorInfoEndpointShouldBeAccessibleNoAuth() {
        webTestClient.get()
                .uri("/actuator/info")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void diagnosticsEndpointShouldBeAccessibleWithAuth() {
        webTestClient.get()
                .uri("/api/v1/diagnostics/cloud/services/all")
                .exchange()
                .expectStatus().isOk();
    }
}