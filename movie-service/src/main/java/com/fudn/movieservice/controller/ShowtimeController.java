package com.fudn.movieservice.controller;

import com.fudn.movieservice.dto.ShowtimeRequest;
import com.fudn.movieservice.dto.ShowtimeResponse;
import com.fudn.movieservice.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {
    private final ShowtimeService showtimeService;

    public ShowtimeController(ShowtimeService showtimeService) {
        this.showtimeService = showtimeService;
    }

    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> search(
            @RequestParam String movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.search(movieId, date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(showtimeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ShowtimeResponse> create(@Valid @RequestBody ShowtimeRequest request) {
        return new ResponseEntity<>(showtimeService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> update(
            @PathVariable String id,
            @Valid @RequestBody ShowtimeRequest request) {
        return ResponseEntity.ok(showtimeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        showtimeService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
