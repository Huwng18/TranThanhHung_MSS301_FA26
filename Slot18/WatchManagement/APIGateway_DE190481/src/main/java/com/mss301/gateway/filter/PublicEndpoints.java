package com.mss301.gateway.filter;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

// [TODO-7.4] Public paths match whole endpoints; writes always need access tokens.
@Component
public class PublicEndpoints {
    public boolean matches(ServerHttpRequest request) {
        String path = request.getPath().value();
        return (HttpMethod.POST.equals(request.getMethod())
                && (path.equals("/api/auth/login") || path.equals("/api/auth/refresh")))
                || (HttpMethod.GET.equals(request.getMethod()) && path.equals("/api/watches"))
                || path.equals("/fallback") || path.startsWith("/fallback/");
    }
}
