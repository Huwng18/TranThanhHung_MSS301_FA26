package com.fudn.apigateway.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Routes {

    @Value("${customer.service.url}")
    private String customerServiceUrl;

    @Value("${movie.service.url}")
    private String movieServiceUrl;

    @Value("${booking.service.url}")
    private String bookingServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("customer_service", r -> r.path("/api/auth/**", "/api/customers/**")
                        .uri(customerServiceUrl))
                .route("movie_service", r -> r.path("/api/genres/**", "/api/rooms/**", "/api/movies/**", "/api/showtimes/**")
                        .uri(movieServiceUrl))
                .route("booking_service", r -> r.path("/api/bookings/**")
                        .uri(bookingServiceUrl))
                .build();
    }
}
