package com.fudn.bookingservice.dto;
import lombok.Builder;
import lombok.Data;
import java.util.List;
@Data
@Builder
public class SeatMapResponse {
    private String showtimeId;
    private int seatRows;
    private int seatsPerRow;
    private List<String> bookedSeats;
}
