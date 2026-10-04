package com.fudn.movieservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShowtimeRequest {
    @NotBlank(message = "Movie ID is required")
    private String movieId;
    
    @NotBlank(message = "Room ID is required")
    private String roomId;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    @NotNull(message = "Ticket price is required")
    @Min(value = 0, message = "Ticket price must be positive")
    private BigDecimal ticketPrice;
}
