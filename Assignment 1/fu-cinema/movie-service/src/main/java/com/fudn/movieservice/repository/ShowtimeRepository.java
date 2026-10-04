package com.fudn.movieservice.repository;

import com.fudn.movieservice.model.Showtime;
import com.fudn.movieservice.model.ShowtimeStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends MongoRepository<Showtime, String> {
    boolean existsByRoomId(String roomId);
    
    long countByRoomIdAndShowtimeStatusAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
            String roomId, ShowtimeStatus status, LocalDateTime endTime, LocalDateTime startTime, String id);

    List<Showtime> findByMovieIdAndStartTimeBetween(String movieId, LocalDateTime startOfDay, LocalDateTime endOfDay);
}
