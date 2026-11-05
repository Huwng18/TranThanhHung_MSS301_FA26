package com.mss301.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

// [TODO-7.3] A public request must not carry a forged administrator identity.
class UserHeaderSanitizerTest {
    @Test
    void removesAllValuesOfCaseInsensitiveClientIdentityHeaders() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/watches")
                .header("x-user-role", "1", "2").header("X-User-Id", "999")
                .header("X-Saga-Fail-After-Step", "2"));
        AtomicReference<org.springframework.http.HttpHeaders> forwarded = new AtomicReference<>();
        new UserHeaderSanitizer().filter(exchange, sanitized -> {
            forwarded.set(sanitized.getRequest().getHeaders());
            return Mono.empty();
        }).block();
        assertThat(forwarded.get().getFirst("X-User-Role")).isNull();
        assertThat(forwarded.get().getFirst("X-User-Id")).isNull();
        assertThat(forwarded.get().getFirst("X-Saga-Fail-After-Step")).isEqualTo("2");
    }
}
