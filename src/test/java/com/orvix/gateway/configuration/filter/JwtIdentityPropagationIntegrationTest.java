package com.orvix.gateway.configuration.filter;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class JwtIdentityPropagationIntegrationTest {

    private static MockWebServer  mockWebServer;

    @BeforeAll
    static void start() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8080);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldPropagateHeadersFromJwt2() throws Exception {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody("[]")
        );

        webTestClient
                .mutateWith(mockJwt()
                        .jwt(jwt -> jwt
                                .subject("123")
                                .claim("username", "boris")
                                .claim("realm_access",
                                        Map.of("roles",
                                                List.of("ADMIN", "USER")))))
                .get()
                .uri("/api/v1/diagnostics/gateway/services/all")
                .exchange()
                .expectStatus().isOk();

        RecordedRequest request = mockWebServer.takeRequest();

        assertEquals("123", request.getHeader("X-User-Id"));
        assertEquals("boris", request.getHeader("X-Username"));
        assertEquals("ADMIN,USER", request.getHeader("X-User-Roles"));
    }
}