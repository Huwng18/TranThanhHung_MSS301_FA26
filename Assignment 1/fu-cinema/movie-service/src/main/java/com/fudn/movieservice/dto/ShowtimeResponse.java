package com.fudn.movieservice.dto;

import com.fudn.movieservice.model.ShowtimeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ShowtimeResponse {
    private String id;
    private String movieId;
    private String movieTitle;
    private String roomId;
    private String roomName;
    private int seatRows;
    private int seatsPerRow;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal ticketPrice;
    private ShowtimeStatus showtimeStatus;
}
