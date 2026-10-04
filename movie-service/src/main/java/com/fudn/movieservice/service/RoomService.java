package com.fudn.movieservice.service;
import com.fudn.movieservice.dto.RoomRequest;
import com.fudn.movieservice.exception.ApiException;
import com.fudn.movieservice.model.CinemaRoom;
import com.fudn.movieservice.repository.CinemaRoomRepository;
import com.fudn.movieservice.repository.ShowtimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {
    private final CinemaRoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;

    public RoomService(CinemaRoomRepository roomRepository, ShowtimeRepository showtimeRepository) {
        this.roomRepository = roomRepository;
        this.showtimeRepository = showtimeRepository;
    }

    public List<CinemaRoom> getAll() {
        return roomRepository.findAll();
    }

    public CinemaRoom getById(String id) {
        return roomRepository.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));
    }

    public CinemaRoom create(RoomRequest request) {
        if (roomRepository.existsByRoomName(request.getRoomName())) {
            throw new ApiException(HttpStatus.CONFLICT, "Room name already exists");
        }
        CinemaRoom room = CinemaRoom.builder()
                .roomName(request.getRoomName())
                .roomType(request.getRoomType())
                .seatRows(request.getSeatRows())
                .seatsPerRow(request.getSeatsPerRow())
                .totalSeats(request.getSeatRows() * request.getSeatsPerRow())
                .status(request.getStatus())
                .build();
        return roomRepository.save(room);
    }

    public CinemaRoom update(String id, RoomRequest request) {
        CinemaRoom room = getById(id);
        if (!room.getRoomName().equals(request.getRoomName()) && roomRepository.existsByRoomName(request.getRoomName())) {
            throw new ApiException(HttpStatus.CONFLICT, "Room name already exists");
        }
        room.setRoomName(request.getRoomName());
        room.setRoomType(request.getRoomType());
        room.setSeatRows(request.getSeatRows());
        room.setSeatsPerRow(request.getSeatsPerRow());
        room.setTotalSeats(request.getSeatRows() * request.getSeatsPerRow());
        room.setStatus(request.getStatus());
        return roomRepository.save(room);
    }

    public void delete(String id) {
        CinemaRoom room = getById(id);
        if (showtimeRepository.existsByRoomId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot delete room because it has showtimes (BR03)");
        }
        roomRepository.delete(room);
    }
}
