package com.mss301.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

// [TODO-7.5] Verify signature, expiry and access type before forwarding identity.
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
    private final PublicEndpoints publicEndpoints;
    private final JwtParser parser;

    public JwtAuthFilter(PublicEndpoints publicEndpoints, @Value("${jwt.secret}") String secret) {
        this.publicEndpoints = publicEndpoints;
        this.parser = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (publicEndpoints.matches(exchange.getRequest())) {
            return chain.filter(exchange);
        }
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }
        Claims claims;
        try {
            claims = parser.parseSignedClaims(authorization.substring(7)).getPayload();
            Integer role = claims.get("role", Integer.class);
            if (!"access".equals(claims.get("type", String.class))
                    || claims.getSubject() == null || claims.getSubject().isBlank()
                    || claims.getExpiration() == null || role == null || role < 1 || role > 3) {
                return unauthorized(exchange);
            }
        } catch (JwtException | IllegalArgumentException exception) {
            return unauthorized(exchange);
        }
        var request = exchange.getRequest().mutate().headers(headers -> {
            headers.set(UserHeaderSanitizer.USER_ID, claims.getSubject());
            headers.set(UserHeaderSanitizer.USER_ROLE, String.valueOf(claims.get("role", Integer.class)));
        }).build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
