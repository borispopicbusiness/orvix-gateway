package com.orvix.gateway.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class DiagnosticsControllerTest {

    @MockitoBean
    private DiscoveryClient discoveryClient;

    @Test
    void getCloudServices() {
        Mockito.when(discoveryClient.getServices()).thenReturn(List.of("service-a", "service-b"));

        DiagnosticsController controller = new DiagnosticsController(discoveryClient);

        Mono<List<String>> result = controller.getCloudServices();

        StepVerifier.create(result)
                .expectNext(List.of("service-a", "service-b"))
                .verifyComplete();

        Mockito.verify(discoveryClient, Mockito.times(1)).getServices();
    }
}