package com.fudn.movieservice.dto;

import com.fudn.movieservice.model.MovieStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MovieRequest {
    @NotBlank(message = "Title is required")
    private String movieTitle;
    
    @NotBlank(message = "Director is required")
    private String movieDirector;
    
    @Min(value = 1, message = "Duration must be greater than 0")
    private int durationMinutes;
    
    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;
    
    @NotBlank(message = "Genre ID is required")
    private String genreId;
    
    @NotBlank(message = "Language is required")
    private String language;
    
    @NotNull(message = "Status is required")
    private MovieStatus status;
}
