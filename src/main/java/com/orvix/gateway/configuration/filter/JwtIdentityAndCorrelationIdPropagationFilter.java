package com.orvix.gateway.configuration.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Global gateway filter responsible for propagating authenticated user
 * identity information from JWT claims into downstream HTTP headers.
 *
 * <p>
 * This filter extracts selected claims from the authenticated JWT issued
 * by Keycloak and forwards them as trusted internal headers so downstream
 * microservices do not need to parse JWTs themselves.
 * </p>
 *
 * <p>
 * The following headers are added:
 * </p>
 *
 * <ul>
 *     <li>{@code X-User-Id} - User identifier extracted from the JWT subject</li>
 *     <li>{@code X-Username} - Preferred username</li>
 *     <li>{@code X-User-Roles} - Comma-separated list of realm roles</li>
 * </ul>
 *
 * <p>
 * If no authenticated JWT principal exists, the request continues unchanged.
 * </p>
 *
 * <p>
 * This filter is intended for internal trusted microservice communication only.
 * Downstream services must trust only headers added by the gateway.
 * </p>
 */
@Component
public class JwtIdentityAndCorrelationIdPropagationFilter implements GlobalFilter {

    private static final String HEADER = "X-Correlation-Id";

    /**
     * Extracts JWT claims from the authenticated principal and propagates them
     * as HTTP headers to downstream services.
     *
     * @param exchange current server web exchange
     * @param chain gateway filter chain
     * @return completion signal for request processing
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(authenticationToken -> {

                    Jwt jwt = authenticationToken.getToken();

                    String userId = jwt.getSubject();
                    String username = jwt.getClaimAsString("username");

                    Map<String, Object> realmAccess = jwt.getClaim("realm_access");

                    List<String> roles = realmAccess != null
                            ? (List<String>) realmAccess.get("roles")
                            : List.of();

                    String correlationId = exchange.getRequest().getHeaders().getFirst(HEADER);

                    if(correlationId == null || correlationId.isEmpty()) {
                        correlationId = UUID.randomUUID().toString().replace("-", "");
                    }

                    ServerHttpRequest mutatedRequest =
                            exchange.getRequest()
                                    .mutate()
                                    .header("X-User-Id", userId)
                                    .header("X-Username", username)
                                    .header("X-User-Roles",
                                            String.join(",", roles)
                                    ).header("X-Correlation-Id", correlationId)
                                    .build();

                    return chain.filter(
                            exchange.mutate()
                                    .request(mutatedRequest)
                                    .build()
                    );
                }).switchIfEmpty(chain.filter(exchange));
    }
}
