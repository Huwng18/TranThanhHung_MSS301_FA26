package com.fudn.movieservice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@Builder
@Document(collection = "movies")
public class Movie {
    @Id
    private String id;
    private String movieTitle;
    private String movieDirector;
    private int durationMinutes;
    private LocalDate releaseDate;
    private String genreId;
    private String language;
    private MovieStatus status;
}
