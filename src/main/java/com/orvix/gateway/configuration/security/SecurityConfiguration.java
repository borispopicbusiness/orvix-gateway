package com.orvix.gateway.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {
    @Bean
    public SecurityWebFilterChain securityWebFluxFilterChain(ServerHttpSecurity http) {
         http
             .csrf(ServerHttpSecurity.CsrfSpec::disable)
             .authorizeExchange(auth -> auth
                     .pathMatchers("/actuator/**", "/api/v1/diagnostics/**").permitAll()
                     .anyExchange().authenticated()
             )
             .httpBasic(Customizer.withDefaults());

         return http.build();
    }
}