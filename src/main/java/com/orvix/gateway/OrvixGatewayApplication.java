package com.orvix.gateway;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Orvix Gateway microservice",
                version = "0.1",
                description = "API documentation for Orvix Gateway Microservice"
        )
)
@SpringBootApplication
public class OrvixGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrvixGatewayApplication.class, args);
    }
}
