package com.fudn.bookingservice.controller;

import com.fudn.bookingservice.dto.BookingRequest;
import com.fudn.bookingservice.dto.BookingResponse;
import com.fudn.bookingservice.dto.SeatMapResponse;
import com.fudn.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
