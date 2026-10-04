package com.fudn.movieservice.service;
import com.fudn.movieservice.dto.GenreRequest;
import com.fudn.movieservice.exception.ApiException;
import com.fudn.movieservice.model.Genre;
import com.fudn.movieservice.repository.GenreRepository;
import com.fudn.movieservice.repository.MovieRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GenreService {
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;

    public GenreService(GenreRepository genreRepository, MovieRepository movieRepository) {
        this.genreRepository = genreRepository;
        this.movieRepository = movieRepository;
    }

    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    public Genre getById(String id) {
        return genreRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Genre not found"));
    }

    public Genre create(GenreRequest request) {
        if (genreRepository.existsByGenreName(request.getGenreName())) {
            throw new ApiException(HttpStatus.CONFLICT, "Genre name already exists");
        }
        Genre genre = Genre.builder()
                .genreName(request.getGenreName())
                .description(request.getDescription())
                .build();
        return genreRepository.save(genre);
    }

    public Genre update(String id, GenreRequest request) {
        Genre genre = getById(id);
        if (!genre.getGenreName().equals(request.getGenreName()) && genreRepository.existsByGenreName(request.getGenreName())) {
            throw new ApiException(HttpStatus.CONFLICT, "Genre name already exists");
        }
        genre.setGenreName(request.getGenreName());
        genre.setDescription(request.getDescription());
        return genreRepository.save(genre);
    }

    public void delete(String id) {
        Genre genre = getById(id);
        if (movieRepository.existsByGenreId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete genre because it has movies (BR03)");
        }
        genreRepository.delete(genre);
    }
}
