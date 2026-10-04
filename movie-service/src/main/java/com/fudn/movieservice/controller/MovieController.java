package com.fudn.movieservice.controller;

import com.fudn.movieservice.dto.MovieRequest;
import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.MovieStatus;
import com.fudn.movieservice.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<Movie>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String genreId,
            @RequestParam(required = false) MovieStatus status) {
        return ResponseEntity.ok(movieService.search(keyword, genreId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getById(@PathVariable String id) {
        return ResponseEntity.ok(movieService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Movie> create(@Valid @RequestBody MovieRequest request) {
        return new ResponseEntity<>(movieService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movie> update(@PathVariable String id, @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.ok(movieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
