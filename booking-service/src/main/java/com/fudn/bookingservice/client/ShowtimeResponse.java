package com.fudn.bookingservice.client;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
    private String showtimeStatus;
}
