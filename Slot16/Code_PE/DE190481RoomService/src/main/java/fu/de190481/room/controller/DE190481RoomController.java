package fu.de190481.room.controller;

import fu.de190481.room.dto.ApiResponse;
import fu.de190481.room.dto.PageDTO;
import fu.de190481.room.dto.RoomDTO;
import fu.de190481.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room Management", description = "Endpoints for managing rooms")
public class DE190481RoomController {

    private final RoomService roomService;

    @Autowired
    public DE190481RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @Operation(summary = "Create a new room")
    public ResponseEntity<ApiResponse<RoomDTO>> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        ApiResponse<RoomDTO> response = roomService.createRoom(roomDTO);
        if (response.getStatus() == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (response.getStatus() == 3) {
            return ResponseEntity.status(HttpStatus.valueOf(226)).body(response);
        } else if (response.getStatus() == 2) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{roomId}")
    @Operation(summary = "Update an existing room")
    public ResponseEntity<ApiResponse<RoomDTO>> updateRoom(@PathVariable("roomId") Integer roomId,
                                                           @RequestBody RoomDTO roomDTO) {
        ApiResponse<RoomDTO> response = roomService.updateRoom(roomId, roomDTO);
        if (response.getStatus() == 1) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 4) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (response.getStatus() == 3) {
            return ResponseEntity.status(HttpStatus.valueOf(226)).body(response);
        } else if (response.getStatus() == 2) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get room details by ID")
    public ResponseEntity<ApiResponse<RoomDTO>> getRoomById(@PathVariable("roomId") Integer roomId) {
        ApiResponse<RoomDTO> response = roomService.getRoomById(roomId);
        if (response.getStatus() == 1) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 4) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "Soft delete a room (change status to MAINTENANCE)")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable("roomId") Integer roomId) {
        ApiResponse<Void> response = roomService.deleteRoom(roomId);
        if (response.getStatus() == 1) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 4) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    @Operation(summary = "Get paginated rooms with optional filters")
    public ResponseEntity<ApiResponse<PageDTO<RoomDTO>>> getRooms(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "roomType", required = false) String roomType,
            @RequestParam(name = "status", required = false) String status) {
        ApiResponse<PageDTO<RoomDTO>> response = roomService.getRooms(page, size, roomType, status);
        if (response.getStatus() == 1) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 2) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
