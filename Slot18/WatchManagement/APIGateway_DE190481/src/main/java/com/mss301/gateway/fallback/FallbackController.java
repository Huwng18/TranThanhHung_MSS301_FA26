package com.mss301.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

// [TODO-7.2] Return a clear 503 when a downstream service cannot be reached.
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    @RequestMapping("/account")
    public ResponseEntity<Map<String, Object>> account() {
        return unavailable("Account");
    }

    @RequestMapping("/brand")
    public ResponseEntity<Map<String, Object>> brand() {
        return unavailable("Brand");
    }

    @RequestMapping("/watch")
    public ResponseEntity<Map<String, Object>> watch() {
        return unavailable("Watch");
    }

    private ResponseEntity<Map<String, Object>> unavailable(String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "success", false,
                "message", service + " Service is currently unavailable. Please try again later.",
                "timestamp", Instant.now().toString()));
    }
}
