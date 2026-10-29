package fu.de190481.room.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RoomDTO {

    private Integer roomId;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Room type is required")
    @Pattern(regexp = "^(SINGLE|DOUBLE|SUITE|DELUXE)$", message = "Room type must be SINGLE, DOUBLE, SUITE, or DELUXE")
    private String roomType;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Price per night must be greater than 0")
    private BigDecimal pricePerNight;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be between 1 and 10")
    @Max(value = 10, message = "Capacity must be between 1 and 10")
    private Integer capacity;

    @NotNull(message = "Floor is required")
    @Min(value = 1, message = "Floor must be at least 1")
    private Integer floor;

    @Pattern(regexp = "^(AVAILABLE|OCCUPIED|MAINTENANCE)$", message = "Status must be AVAILABLE, OCCUPIED, or MAINTENANCE")
    private String status;

    public RoomDTO() {
    }

    public RoomDTO(Integer roomId, String roomNumber, String roomType, BigDecimal pricePerNight, Integer capacity, Integer floor, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.capacity = capacity;
        this.floor = floor;
        this.status = status;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
