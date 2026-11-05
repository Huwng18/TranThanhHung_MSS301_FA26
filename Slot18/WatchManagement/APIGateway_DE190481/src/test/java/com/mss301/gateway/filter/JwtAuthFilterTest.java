package com.mss301.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

// [TODO-7.5] Only signed, unexpired access tokens may set downstream identity.
class JwtAuthFilterTest {
    private static final String SECRET = "gateway-test-secret-at-least-thirty-two-bytes-long";
    private final JwtAuthFilter filter = new JwtAuthFilter(new PublicEndpoints(), SECRET);

    @Test
    void replacesForgedIdentityWithVerifiedClaims() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/brands")
                .header("Authorization", "Bearer " + token("access", Instant.now().plusSeconds(60), SECRET))
                .header("X-User-Id", "999").header("X-User-Role", "1"));
        AtomicReference<org.springframework.http.HttpHeaders> headers = new AtomicReference<>();
        filter.filter(exchange, authenticated -> {
            headers.set(authenticated.getRequest().getHeaders());
            return Mono.empty();
        }).block();
        assertThat(headers.get().get("X-User-Id")).containsExactly("2");
        assertThat(headers.get().get("X-User-Role")).containsExactly("2");
    }

    @Test
    void rejectsRefreshExpiredMalformedAndWrongSignatureTokens() {
        assertRejected(token("refresh", Instant.now().plusSeconds(60), SECRET));
        assertRejected(token("access", Instant.now().minusSeconds(60), SECRET));
        assertRejected(token("access", Instant.now().plusSeconds(60), SECRET + "different"));
        assertRejected("not.a.jwt");
        assertRejected(Jwts.builder().subject("2").claim("role", 2).claim("type", "access")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact());
    }

    @Test
    void missingAuthorizationCannotReachProtectedDownstream() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.post("/api/watches")
                .header("X-User-Role", "1"));
        filter.filter(exchange, ignored -> {
            throw new AssertionError("Unauthorized request reached downstream");
        }).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private void assertRejected(String token) {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/brands")
                .header("Authorization", "Bearer " + token));
        filter.filter(exchange, ignored -> {
            throw new AssertionError("Invalid token reached downstream");
        }).block();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String token(String type, Instant expiration, String secret) {
        return Jwts.builder().subject("2").claim("role", 2).claim("type", type)
                .expiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).compact();
    }
}
