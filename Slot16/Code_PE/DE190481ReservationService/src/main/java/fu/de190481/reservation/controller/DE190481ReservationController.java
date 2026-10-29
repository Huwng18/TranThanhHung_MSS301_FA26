package fu.de190481.reservation.controller;

import fu.de190481.reservation.dto.ApiResponse;
import fu.de190481.reservation.dto.PageDTO;
import fu.de190481.reservation.dto.ReservationDTO;
import fu.de190481.reservation.dto.ReservationDetailDTO;
import fu.de190481.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservation Management", description = "Endpoints for managing reservations")
public class DE190481ReservationController {

    private final ReservationService reservationService;

    @Autowired
    public DE190481ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation")
    public ResponseEntity<ApiResponse<ReservationDTO>> createReservation(@Valid @RequestBody ReservationDTO reservationDTO) {
        ApiResponse<ReservationDTO> response = reservationService.createReservation(reservationDTO);
        if (response.getStatus() == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (response.getStatus() == 4) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (response.getStatus() == 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (response.getStatus() == 2) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{reservationId}")
    @Operation(summary = "Update an existing reservation")
    public ResponseEntity<ApiResponse<ReservationDTO>> updateReservation(@PathVariable("reservationId") Integer reservationId,
                                                                         @RequestBody ReservationDTO reservationDTO) {
        ApiResponse<ReservationDTO> response = reservationService.updateReservation(reservationId, reservationDTO);
        if (response.getStatus() == 1) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 4) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (response.getStatus() == 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (response.getStatus() == 2) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Get reservation details by ID (including room information)")
    public ResponseEntity<ApiResponse<ReservationDetailDTO>> getReservationById(@PathVariable("reservationId") Integer reservationId) {
        ApiResponse<ReservationDetailDTO> response = reservationService.getReservationById(reservationId);
        if (response.getStatus() == 1) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 4) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancel a reservation (soft delete, change status to CANCELLED)")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable("reservationId") Integer reservationId) {
        ApiResponse<Void> response = reservationService.deleteReservation(reservationId);
        if (response.getStatus() == 1) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 4) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    @Operation(summary = "Get paginated reservations with optional filters")
    public ResponseEntity<ApiResponse<PageDTO<ReservationDetailDTO>>> getReservations(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "guestName", required = false) String guestName,
            @RequestParam(name = "status", required = false) String status) {
        ApiResponse<PageDTO<ReservationDetailDTO>> response = reservationService.getReservations(page, size, guestName, status);
        if (response.getStatus() == 1) {
            return ResponseEntity.ok(response);
        } else if (response.getStatus() == 2) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
