package com.fudn.movieservice.dto;
import com.fudn.movieservice.model.RoomStatus;
import com.fudn.movieservice.model.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class RoomRequest {
    @NotBlank(message = "Room name is required")
    private String roomName;
    @NotNull(message = "Room type is required")
    private RoomType roomType;
    @Min(value = 1, message = "Seat rows must be at least 1")
    private int seatRows;
    @Min(value = 1, message = "Seats per row must be at least 1")
    private int seatsPerRow;
    @NotNull(message = "Status is required")
    private RoomStatus status;
}
