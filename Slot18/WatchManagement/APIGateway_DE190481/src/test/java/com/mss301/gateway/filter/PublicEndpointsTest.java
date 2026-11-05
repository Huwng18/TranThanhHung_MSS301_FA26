package com.mss301.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

// [TODO-7.4] Exact path and method matching protects write and monitoring APIs.
class PublicEndpointsTest {
    private final PublicEndpoints policy = new PublicEndpoints();

    @Test
    void permitsListingAndLoginButProtectsWritesAndPrefixLookalikes() {
        assertPublic(HttpMethod.GET, "/api/watches", true);
        assertPublic(HttpMethod.POST, "/api/auth/login", true);
        assertPublic(HttpMethod.POST, "/api/auth/refresh", true);
        assertPublic(HttpMethod.GET, "/fallback/watch", true);
        assertPublic(HttpMethod.POST, "/api/watches", false);
        assertPublic(HttpMethod.PUT, "/api/watches/1", false);
        assertPublic(HttpMethod.DELETE, "/api/watches/1", false);
        assertPublic(HttpMethod.GET, "/api/sagas", false);
        assertPublic(HttpMethod.POST, "/api/auth/login-admin", false);
        assertPublic(HttpMethod.GET, "/api/auth/login", false);
        assertPublic(HttpMethod.GET, "/api/watches/1", false);
    }

    private void assertPublic(HttpMethod method, String path, boolean expected) {
        assertThat(policy.matches(MockServerHttpRequest.method(method, path).build()))
                .as("%s %s", method, path).isEqualTo(expected);
    }
}
