package com.fudn.bookingservice.controller;

import com.fudn.bookingservice.dto.BookingRequest;
import com.fudn.bookingservice.dto.BookingResponse;
import com.fudn.bookingservice.dto.SeatMapResponse;
import com.fudn.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.fudn.bookingservice.dto.ReportResponse;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader("X-User-Id") Long customerId,
            @Valid @RequestBody BookingRequest request) {
        return new ResponseEntity<>(bookingService.createBooking(customerId, request), HttpStatus.CREATED);
    }

    @GetMapping("/showtimes/{id}/seats")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable String id) {
        return ResponseEntity.ok(bookingService.getSeatMap(id));
    }
    @GetMapping("/report")
    public ResponseEntity<ReportResponse> getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("X-User-Role") String role) {
        if (!"ROLE_ADMIN".equals(role)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(bookingService.report(startDate, endDate));
    }

    @GetMapping("/my")
    public ResponseEntity<java.util.List<BookingResponse>> getMyBookings(@RequestHeader("X-User-Id") Long customerId) {
        return ResponseEntity.ok(bookingService.getMyBookings(customerId));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long customerId,
            @RequestHeader(value = "X-User-Role", defaultValue = "ROLE_USER") String role) {
        return ResponseEntity.ok(bookingService.cancel(id, customerId, role));
    }
}
