package com.fudn.movieservice.repository;
import com.fudn.movieservice.model.CinemaRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
public interface CinemaRoomRepository extends MongoRepository<CinemaRoom, String> {
    boolean existsByRoomName(String roomName);
}
