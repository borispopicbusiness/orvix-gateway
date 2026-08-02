package com.orvix.gateway.configuration.filter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class JwtIdentityPropagationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldPropagateHeadersFromJwt() {

        webTestClient
                .mutateWith(mockJwt()
                        .jwt(jwt -> jwt
                                .subject("123")
                                .claim("username", "boris")
                                .claim("email", "boris@test.com")
                                .claim("realm_access",
                                        Map.of("roles", List.of("ADMIN", "USER")))
                        )
                )
                .get()
                .uri("/api/v1/diagnostics/gateway/services/all")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-User-Id", "123")
                .expectHeader().valueEquals("X-Username", "boris")
                .expectHeader().valueEquals("X-User-Roles", "ADMIN,USER");
    }
}