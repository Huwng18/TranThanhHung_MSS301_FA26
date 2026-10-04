package com.fudn.movieservice.service;

import com.fudn.movieservice.dto.MovieRequest;
import com.fudn.movieservice.exception.ApiException;
import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.MovieStatus;
import com.fudn.movieservice.repository.GenreRepository;
import com.fudn.movieservice.repository.MovieRepository;
import com.fudn.movieservice.repository.ShowtimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ShowtimeRepository showtimeRepository;

    public MovieService(MovieRepository movieRepository, GenreRepository genreRepository, ShowtimeRepository showtimeRepository) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.showtimeRepository = showtimeRepository;
    }

    public List<Movie> search(String keyword, String genreId, MovieStatus status) {
        return movieRepository.searchMovies(keyword, genreId, status);
    }

    public Movie getById(String id) {
        return movieRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    public Movie create(MovieRequest request) {
        if (!genreRepository.existsById(request.getGenreId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Genre not found");
        }
        Movie movie = Movie.builder()
                .movieTitle(request.getMovieTitle())
                .movieDirector(request.getMovieDirector())
                .durationMinutes(request.getDurationMinutes())
                .releaseDate(request.getReleaseDate())
                .genreId(request.getGenreId())
                .language(request.getLanguage())
                .status(request.getStatus())
                .build();
        return movieRepository.save(movie);
    }

    public Movie update(String id, MovieRequest request) {
        Movie movie = getById(id);
        if (!genreRepository.existsById(request.getGenreId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Genre not found");
        }
        movie.setMovieTitle(request.getMovieTitle());
        movie.setMovieDirector(request.getMovieDirector());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setGenreId(request.getGenreId());
        movie.setLanguage(request.getLanguage());
        movie.setStatus(request.getStatus());
        return movieRepository.save(movie);
    }

    public void delete(String id) {
        Movie movie = getById(id);
        // Note: For BR03, you can't delete if it has showtimes, but typically we do soft delete for movies or check showtimes
        movie.setStatus(MovieStatus.INACTIVE);
        movieRepository.save(movie);
    }
}
