package com.fudn.bookingservice.service;

import com.fudn.bookingservice.client.MovieClient;
import com.fudn.bookingservice.client.ShowtimeResponse;
import com.fudn.bookingservice.dto.BookingDetailResponse;
import com.fudn.bookingservice.dto.BookingRequest;
import com.fudn.bookingservice.dto.BookingResponse;
import com.fudn.bookingservice.dto.SeatMapResponse;
import com.fudn.bookingservice.model.Booking;
import com.fudn.bookingservice.model.BookingDetail;
import com.fudn.bookingservice.model.BookingStatus;
import com.fudn.bookingservice.repository.BookingDetailRepository;
import com.fudn.bookingservice.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingDetailRepository detailRepository;
    private final MovieClient movieClient;

    public BookingService(BookingRepository bookingRepository, BookingDetailRepository detailRepository, MovieClient movieClient) {
        this.bookingRepository = bookingRepository;
        this.detailRepository = detailRepository;
        this.movieClient = movieClient;
    }

    public SeatMapResponse getSeatMap(String showtimeId) {
        ShowtimeResponse showtime = movieClient.getShowtimeById(showtimeId);
        if (showtime == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found");
        }
        List<String> bookedSeats = detailRepository.findByShowtimeId(showtimeId).stream()
                .filter(d -> d.getBooking().getBookingStatus() == BookingStatus.CONFIRMED)
                .map(BookingDetail::getSeatCode)
                .collect(Collectors.toList());

        return SeatMapResponse.builder()
                .showtimeId(showtimeId)
                .seatRows(showtime.getSeatRows())
                .seatsPerRow(showtime.getSeatsPerRow())
                .bookedSeats(bookedSeats)
                .build();
    }

    @Transactional
    public BookingResponse createBooking(Long customerId, BookingRequest request) {
        Map<String, ShowtimeResponse> cache = new HashMap<>();
        Map<String, List<String>> bookedCache = new HashMap<>();

        Booking booking = Booking.builder()
                .customerId(customerId)
                .bookingDate(LocalDateTime.now())
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (BookingRequest.BookingItem item : request.getItems()) {
            String showtimeId = item.getShowtimeId();
            String seatCode = item.getSeatCode();

            if (!cache.containsKey(showtimeId)) {
                try {
                    ShowtimeResponse showtime = movieClient.getShowtimeById(showtimeId);
                    cache.put(showtimeId, showtime);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Showtime not found: " + showtimeId);
                }
                
                List<String> booked = detailRepository.findByShowtimeId(showtimeId).stream()
                    .filter(d -> d.getBooking().getBookingStatus() == BookingStatus.CONFIRMED)
                    .map(BookingDetail::getSeatCode)
                    .collect(Collectors.toList());
                bookedCache.put(showtimeId, booked);
            }

            ShowtimeResponse showtime = cache.get(showtimeId);
            List<String> bookedSeats = bookedCache.get(showtimeId);
            
            long countInReq = request.getItems().stream()
                .filter(i -> i.getShowtimeId().equals(showtimeId) && i.getSeatCode().equals(seatCode))
                .count();
            if (countInReq > 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate seat in request: " + seatCode);
            }

            if (bookedSeats.contains(seatCode)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already taken: " + seatCode);
            }

            int rowNumber = seatCode.charAt(0) - 'A' + 1;
            int colNumber = Integer.parseInt(seatCode.substring(1));
            if (rowNumber > showtime.getSeatRows() || colNumber > showtime.getSeatsPerRow()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat outside layout: " + seatCode);
            }

            BookingDetail detail = BookingDetail.builder()
                    .showtimeId(showtimeId)
                    .movieId(showtime.getMovieId())
                    .movieTitle(showtime.getMovieTitle())
                    .roomName(showtime.getRoomName())
                    .seatCode(seatCode)
                    .ticketPrice(showtime.getTicketPrice())
                    .build();
            booking.addDetail(detail);

            total = total.add(showtime.getTicketPrice());
            bookedSeats.add(seatCode);
        }

        booking.setTotalPrice(total);
        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<BookingDetailResponse> resDetails = booking.getDetails().stream().map(d -> BookingDetailResponse.builder()
                .showtimeId(d.getShowtimeId())
                .movieId(d.getMovieId())
                .movieTitle(d.getMovieTitle())
                .roomName(d.getRoomName())
                .seatCode(d.getSeatCode())
                .ticketPrice(d.getTicketPrice())
                .build()).collect(Collectors.toList());

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .customerId(booking.getCustomerId())
                .bookingDate(booking.getBookingDate())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus().name())
                .details(resDetails)
                .build();
    }
    public List<BookingResponse> getMyBookings(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByBookingDateDesc(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    private Booking findAccessible(Long id, Long userId, String role) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!"ROLE_ADMIN".equals(role) && !booking.getCustomerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied (BR11)");
        }
        return booking;
    }
    public BookingResponse getById(Long id, Long userId, String role) {
        return mapToResponse(findAccessible(id, userId, role));
    }
}
