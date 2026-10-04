package com.fudn.bookingservice.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
@Data
@Builder
public class BookingDetailResponse {
    private String showtimeId;
    private String movieId;
    private String movieTitle;
    private String roomName;
    private String seatCode;
    private BigDecimal ticketPrice;
}
