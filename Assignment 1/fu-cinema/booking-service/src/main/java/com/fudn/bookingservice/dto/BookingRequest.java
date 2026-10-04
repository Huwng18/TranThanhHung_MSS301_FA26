package com.fudn.bookingservice.dto;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;
@Data
public class BookingRequest {
    @NotEmpty(message = "Items cannot be empty")
    private List<BookingItem> items;
    
    @Data
    public static class BookingItem {
        private String showtimeId;
        private String seatCode;
    }
}
