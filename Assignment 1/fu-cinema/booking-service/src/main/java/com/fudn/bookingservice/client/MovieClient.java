package com.fudn.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "movie-service", url = "${movie.service.url:http://localhost:8082}")
public interface MovieClient {
    @GetMapping("/api/showtimes/{id}")
    ShowtimeResponse getShowtimeById(@PathVariable("id") String id);
}
