package com.fudn.movieservice.service;

import com.fudn.movieservice.dto.ShowtimeRequest;
import com.fudn.movieservice.dto.ShowtimeResponse;
import com.fudn.movieservice.exception.ApiException;
import com.fudn.movieservice.model.CinemaRoom;
import com.fudn.movieservice.model.Movie;
import com.fudn.movieservice.model.Showtime;
import com.fudn.movieservice.model.ShowtimeStatus;
import com.fudn.movieservice.repository.CinemaRoomRepository;
import com.fudn.movieservice.repository.MovieRepository;
import com.fudn.movieservice.repository.ShowtimeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final CinemaRoomRepository roomRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository, MovieRepository movieRepository, CinemaRoomRepository roomRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.roomRepository = roomRepository;
    }

    public ShowtimeResponse getById(String id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Showtime not found"));
        return mapToResponse(showtime);
    }

    public List<ShowtimeResponse> search(String movieId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return showtimeRepository.findByMovieIdAndStartTimeBetween(movieId, startOfDay, endOfDay)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ShowtimeResponse create(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found (BR15)"));
        if (!roomRepository.existsById(request.getRoomId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Room not found (BR15)");
        }

        LocalDateTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes());

        long overlaps = showtimeRepository.countByRoomIdAndShowtimeStatusAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                request.getRoomId(), ShowtimeStatus.SCHEDULED, endTime, request.getStartTime(), "new"
        );
        if (overlaps > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Showtime overlaps with an existing schedule in the same room (BR04)");
        }

        Showtime showtime = Showtime.builder()
                .movieId(request.getMovieId())
                .roomId(request.getRoomId())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .ticketPrice(request.getTicketPrice())
                .showtimeStatus(ShowtimeStatus.SCHEDULED)
                .build();

        return mapToResponse(showtimeRepository.save(showtime));
    }

    public ShowtimeResponse update(String id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Showtime not found"));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found (BR15)"));
        if (!roomRepository.existsById(request.getRoomId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Room not found (BR15)");
        }

        LocalDateTime endTime = request.getStartTime().plusMinutes(movie.getDurationMinutes());

        long overlaps = showtimeRepository.countByRoomIdAndShowtimeStatusAndStartTimeLessThanAndEndTimeGreaterThanAndIdNot(
                request.getRoomId(), ShowtimeStatus.SCHEDULED, endTime, request.getStartTime(), id
        );
        if (overlaps > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Showtime overlaps with an existing schedule in the same room (BR04)");
        }

        showtime.setMovieId(request.getMovieId());
        showtime.setRoomId(request.getRoomId());
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(endTime);
        showtime.setTicketPrice(request.getTicketPrice());

        return mapToResponse(showtimeRepository.save(showtime));
    }

    public void cancel(String id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Showtime not found"));
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot cancel a showtime that has already started (BR06)");
        }
        showtime.setShowtimeStatus(ShowtimeStatus.CANCELLED);
        showtimeRepository.save(showtime);
    }

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        Movie movie = movieRepository.findById(showtime.getMovieId()).orElse(null);
        CinemaRoom room = roomRepository.findById(showtime.getRoomId()).orElse(null);
        
        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovieId())
                .movieTitle(movie != null ? movie.getMovieTitle() : null)
                .roomId(showtime.getRoomId())
                .roomName(room != null ? room.getRoomName() : null)
                .seatRows(room != null ? room.getSeatRows() : 0)
                .seatsPerRow(room != null ? room.getSeatsPerRow() : 0)
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .ticketPrice(showtime.getTicketPrice())
                .showtimeStatus(showtime.getShowtimeStatus())
                .build();
    }
}
