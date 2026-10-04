package com.fudn.movieservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "showtimes")
@CompoundIndex(name = "room_startTime_idx", def = "{'roomId': 1, 'startTime': 1}")
public class Showtime {
    @Id
    private String id;
    private String movieId;
    private String roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal ticketPrice;
    
    private ShowtimeStatus showtimeStatus;
}
