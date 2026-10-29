package fu.de190481.room.service.impl;

import fu.de190481.room.dto.ApiResponse;
import fu.de190481.room.dto.PageDTO;
import fu.de190481.room.dto.RoomDTO;
import fu.de190481.room.entity.Room;
import fu.de190481.room.repository.RoomRepository;
import fu.de190481.room.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    private static final List<String> VALID_ROOM_TYPES = Arrays.asList("SINGLE", "DOUBLE", "SUITE", "DELUXE");
    private static final List<String> VALID_STATUSES = Arrays.asList("AVAILABLE", "OCCUPIED", "MAINTENANCE");

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public ApiResponse<RoomDTO> createRoom(RoomDTO roomDTO) {
        if (roomRepository.existsByRoomNumber(roomDTO.getRoomNumber())) {
            return new ApiResponse<>(3, "Room number already exists", null);
        }

        Room room = new Room();
        room.setRoomNumber(roomDTO.getRoomNumber());
        room.setRoomType(roomDTO.getRoomType());
        room.setPricePerNight(roomDTO.getPricePerNight());
        room.setCapacity(roomDTO.getCapacity());
        room.setFloor(roomDTO.getFloor());
        room.setStatus(roomDTO.getStatus() != null && !roomDTO.getStatus().isBlank() ? roomDTO.getStatus() : "AVAILABLE");

        Room saved = roomRepository.save(room);
        return new ApiResponse<>(1, "Room created successfully", toDto(saved));
    }

    @Override
    public ApiResponse<RoomDTO> updateRoom(Integer roomId, RoomDTO roomDTO) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (optionalRoom.isEmpty()) {
            return new ApiResponse<>(4, "Room not found", null);
        }

        Room room = optionalRoom.get();

        if (roomDTO.getRoomNumber() != null && !roomDTO.getRoomNumber().isBlank()) {
            if (roomRepository.existsByRoomNumberAndRoomIdNot(roomDTO.getRoomNumber(), roomId)) {
                return new ApiResponse<>(3, "Room number already exists", null);
            }
            room.setRoomNumber(roomDTO.getRoomNumber());
        }

        if (roomDTO.getRoomType() != null && !roomDTO.getRoomType().isBlank()) {
            if (!VALID_ROOM_TYPES.contains(roomDTO.getRoomType().toUpperCase())) {
                return new ApiResponse<>(2, "Invalid room type. Must be SINGLE, DOUBLE, SUITE, or DELUXE", null);
            }
            room.setRoomType(roomDTO.getRoomType().toUpperCase());
        }

        if (roomDTO.getPricePerNight() != null) {
            if (roomDTO.getPricePerNight().compareTo(BigDecimal.ZERO) <= 0) {
                return new ApiResponse<>(2, "Price per night must be greater than 0", null);
            }
            room.setPricePerNight(roomDTO.getPricePerNight());
        }

        if (roomDTO.getCapacity() != null) {
            if (roomDTO.getCapacity() < 1 || roomDTO.getCapacity() > 10) {
                return new ApiResponse<>(2, "Capacity must be between 1 and 10", null);
            }
            room.setCapacity(roomDTO.getCapacity());
        }

        if (roomDTO.getFloor() != null) {
            if (roomDTO.getFloor() < 1) {
                return new ApiResponse<>(2, "Floor must be at least 1", null);
            }
            room.setFloor(roomDTO.getFloor());
        }

        if (roomDTO.getStatus() != null && !roomDTO.getStatus().isBlank()) {
            if (!VALID_STATUSES.contains(roomDTO.getStatus().toUpperCase())) {
                return new ApiResponse<>(2, "Invalid status. Must be AVAILABLE, OCCUPIED, or MAINTENANCE", null);
            }
            room.setStatus(roomDTO.getStatus().toUpperCase());
        }

        Room updated = roomRepository.save(room);
        return new ApiResponse<>(1, "Room updated successfully", toDto(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<RoomDTO> getRoomById(Integer roomId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (optionalRoom.isEmpty()) {
            return new ApiResponse<>(4, "Room not found", null);
        }
        return new ApiResponse<>(1, "Room retrieved successfully", toDto(optionalRoom.get()));
    }

    @Override
    public ApiResponse<Void> deleteRoom(Integer roomId) {
        Optional<Room> optionalRoom = roomRepository.findById(roomId);
        if (optionalRoom.isEmpty()) {
            return new ApiResponse<>(4, "Room not found", null);
        }

        Room room = optionalRoom.get();
        room.setStatus("MAINTENANCE");
        roomRepository.save(room);
        return new ApiResponse<>(1, "Room status changed to MAINTENANCE", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageDTO<RoomDTO>> getRooms(int page, int size, String roomType, String status) {
        if (page < 0 || size <= 0 || size > 100) {
            return new ApiResponse<>(2, "Invalid pagination: page must be >= 0 and size between 1 and 100", null);
        }

        String filterType = (roomType != null && !roomType.isBlank()) ? roomType.trim() : null;
        String filterStatus = (status != null && !status.isBlank()) ? status.trim() : null;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("roomId").ascending());
        Page<Room> roomPage = roomRepository.searchRooms(filterType, filterStatus, pageRequest);

        List<RoomDTO> dtoList = roomPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        PageDTO<RoomDTO> pageDTO = new PageDTO<>(
                dtoList,
                roomPage.getNumber(),
                roomPage.getSize(),
                roomPage.getTotalElements(),
                roomPage.getTotalPages()
        );

        return new ApiResponse<>(1, "Rooms retrieved successfully", pageDTO);
    }

    private RoomDTO toDto(Room room) {
        return new RoomDTO(
                room.getRoomId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getPricePerNight(),
                room.getCapacity(),
                room.getFloor(),
                room.getStatus()
        );
    }
}
