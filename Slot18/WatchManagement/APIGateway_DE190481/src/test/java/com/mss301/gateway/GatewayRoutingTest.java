package com.mss301.gateway;

import com.sun.net.httpserver.HttpServer;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

// [TODO-7.2] Exercise real Gateway routes and fallback without external services.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTest {
    private static final String SECRET = "gateway-routing-test-secret-at-least-thirty-two-bytes";
    private static final HttpServer DOWNSTREAM = downstream();
    private final HttpClient client = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Autowired
    private TimeLimiterRegistry timeLimiters;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String url = "http://127.0.0.1:" + DOWNSTREAM.getAddress().getPort();
        registry.add("ACCOUNT_SERVICE_URL", () -> url);
        registry.add("WATCH_SERVICE_URL", () -> url);
        registry.add("BRAND_SERVICE_URL", () -> "http://127.0.0.1:1");
        registry.add("jwt.secret", () -> SECRET);
    }

    @AfterAll
    static void stopDownstream() {
        DOWNSTREAM.stop(0);
    }

    @Test
    void publicListStripsClientIdentityAndPreservesPath() throws Exception {
        var response = request("GET", "/api/watches", false);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("GET /api/watches role=null id=null");
    }

    @Test
    void authenticatedSagaMonitoringRoutesToWatch() throws Exception {
        var response = request("GET", "/api/sagas/example", true);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("GET /api/sagas/example role=1 id=1");
    }

    @Test
    void internalParticipantEndpointsAreNotExposed() throws Exception {
        assertThat(request("PUT", "/internal/brands/1/watches/1", true).statusCode()).isEqualTo(404);
    }

    @Test
    void unavailableBrandReturnsStructured503Fallback() throws Exception {
        var response = request("GET", "/api/brands", true);
        assertThat(response.statusCode()).isEqualTo(503);
        assertThat(response.body()).contains("\"success\":false", "Brand Service is currently unavailable");
    }

    @Test
    void publicLoginRoutesToAccountButForgedAdminCannotWrite() throws Exception {
        assertThat(request("POST", "/api/auth/login", false).statusCode()).isEqualTo(200);
        assertThat(request("POST", "/api/watches", false).statusCode()).isEqualTo(401);
    }

    // [TODO-7.6] Verify the applied runtime timeout on all CircuitBreakers.
    @Test
    void everyCircuitBreakerAllowsFifteenSecondsForSagaCompletion() {
        for (String name : new String[]{"accountCB", "brandCB", "watchCB"}) {
            assertThat(timeLimiters.timeLimiter(name).getTimeLimiterConfig().getTimeoutDuration())
                    .as(name).isEqualTo(Duration.ofSeconds(15));
        }
    }

    private HttpResponse<String> request(String method, String path, boolean authenticated) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(20))
                .header("X-User-Role", "1").header("X-User-Id", "999")
                .method(method, HttpRequest.BodyPublishers.noBody());
        if (authenticated) {
            String token = Jwts.builder().subject("1").claim("role", 1).claim("type", "access")
                    .expiration(Date.from(Instant.now().plusSeconds(60)))
                    .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static HttpServer downstream() {
        try {
            var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                String body = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath()
                        + " role=" + exchange.getRequestHeaders().getFirst("X-User-Role")
                        + " id=" + exchange.getRequestHeaders().getFirst("X-User-Id");
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, bytes.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }
}
