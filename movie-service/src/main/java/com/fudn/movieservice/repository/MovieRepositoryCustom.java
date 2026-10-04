package com.fudn.movieservice.repository;

import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.MovieStatus;
import java.util.List;

public interface MovieRepositoryCustom {
    List<Movie> searchMovies(String keyword, String genreId, MovieStatus status);
}
