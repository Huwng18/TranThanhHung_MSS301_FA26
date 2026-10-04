package com.fudn.movieservice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "cinema_rooms")
public class CinemaRoom {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String roomName;
    
    private RoomType roomType;
    private int seatRows;
    private int seatsPerRow;
    private int totalSeats;
    private RoomStatus status;
}
