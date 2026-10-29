package fu.de190481.reservation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import fu.de190481.reservation.client.RoomClient;
import fu.de190481.reservation.dto.*;
import fu.de190481.reservation.entity.Reservation;
import fu.de190481.reservation.repository.ReservationRepository;
import fu.de190481.reservation.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;
    private final ObjectMapper objectMapper;

    private static final List<String> VALID_STATUSES = Arrays.asList("CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED");

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  RoomClient roomClient) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public ApiResponse<ReservationDTO> createReservation(ReservationDTO dto) {
        if (dto.getCheckInDate() == null || dto.getCheckOutDate() == null) {
            return new ApiResponse<>(2, "Validation failed: check-in and check-out dates are required", null);
        }

        if (!dto.getCheckOutDate().isAfter(dto.getCheckInDate())) {
            return new ApiResponse<>(2, "Validation failed: checkOutDate must be after checkInDate", null);
        }

        RoomDTO room = fetchRoom(dto.getRoomId());
        if (room == null) {
            return new ApiResponse<>(4, "Room not found", null);
        }

        if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
            return new ApiResponse<>(5, "Room is not available for reservation", null);
        }

        if (dto.getNumberOfGuests() != null && room.getCapacity() != null && dto.getNumberOfGuests() > room.getCapacity()) {
            return new ApiResponse<>(5, "Number of guests exceeds room capacity (" + room.getCapacity() + ")", null);
        }

        long nights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        BigDecimal totalAmount = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Reservation res = new Reservation();
        res.setRoomId(dto.getRoomId());
        res.setGuestName(dto.getGuestName());
        res.setGuestEmail(dto.getGuestEmail());
        res.setGuestPhone(dto.getGuestPhone());
        res.setCheckInDate(dto.getCheckInDate());
        res.setCheckOutDate(dto.getCheckOutDate());
        res.setNumberOfGuests(dto.getNumberOfGuests());
        res.setTotalAmount(totalAmount);
        res.setStatus(dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus() : "CONFIRMED");

        Reservation saved = reservationRepository.save(res);
        return new ApiResponse<>(1, "Reservation created successfully", toDto(saved));
    }

    @Override
    public ApiResponse<ReservationDTO> updateReservation(Integer reservationId, ReservationDTO dto) {
        Optional<Reservation> optionalRes = reservationRepository.findById(reservationId);
        if (optionalRes.isEmpty()) {
            return new ApiResponse<>(4, "Reservation not found", null);
        }

        Reservation res = optionalRes.get();

        Integer targetRoomId = (dto.getRoomId() != null) ? dto.getRoomId() : res.getRoomId();
        LocalDate targetCheckIn = (dto.getCheckInDate() != null) ? dto.getCheckInDate() : res.getCheckInDate();
        LocalDate targetCheckOut = (dto.getCheckOutDate() != null) ? dto.getCheckOutDate() : res.getCheckOutDate();
        Integer targetGuests = (dto.getNumberOfGuests() != null) ? dto.getNumberOfGuests() : res.getNumberOfGuests();

        if (!targetCheckOut.isAfter(targetCheckIn)) {
            return new ApiResponse<>(2, "Validation failed: checkOutDate must be after checkInDate", null);
        }

        boolean roomChanged = dto.getRoomId() != null && !dto.getRoomId().equals(res.getRoomId());
        boolean datesChanged = (dto.getCheckInDate() != null && !dto.getCheckInDate().equals(res.getCheckInDate()))
                || (dto.getCheckOutDate() != null && !dto.getCheckOutDate().equals(res.getCheckOutDate()));

        RoomDTO room = null;
        if (roomChanged || datesChanged || (dto.getNumberOfGuests() != null && !dto.getNumberOfGuests().equals(res.getNumberOfGuests()))) {
            room = fetchRoom(targetRoomId);
            if (room == null) {
                return new ApiResponse<>(4, "Room not found", null);
            }
            if (roomChanged && !"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
                return new ApiResponse<>(5, "New room is not available for reservation", null);
            }
            if (targetGuests > room.getCapacity()) {
                return new ApiResponse<>(5, "Number of guests exceeds room capacity (" + room.getCapacity() + ")", null);
            }
        }

        if (datesChanged || roomChanged) {
            if (room == null) {
                room = fetchRoom(targetRoomId);
            }
            if (room != null && room.getPricePerNight() != null) {
                long nights = ChronoUnit.DAYS.between(targetCheckIn, targetCheckOut);
                res.setTotalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
            }
        }

        if (dto.getGuestName() != null && !dto.getGuestName().isBlank()) {
            res.setGuestName(dto.getGuestName());
        }
        if (dto.getGuestEmail() != null && !dto.getGuestEmail().isBlank()) {
            res.setGuestEmail(dto.getGuestEmail());
        }
        if (dto.getGuestPhone() != null && !dto.getGuestPhone().isBlank()) {
            res.setGuestPhone(dto.getGuestPhone());
        }
        if (dto.getRoomId() != null) {
            res.setRoomId(dto.getRoomId());
        }
        if (dto.getCheckInDate() != null) {
            res.setCheckInDate(dto.getCheckInDate());
        }
        if (dto.getCheckOutDate() != null) {
            res.setCheckOutDate(dto.getCheckOutDate());
        }
        if (dto.getNumberOfGuests() != null) {
            res.setNumberOfGuests(dto.getNumberOfGuests());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            if (!VALID_STATUSES.contains(dto.getStatus().toUpperCase())) {
                return new ApiResponse<>(2, "Invalid status. Must be CONFIRMED, CHECKED_IN, CHECKED_OUT, or CANCELLED", null);
            }
            res.setStatus(dto.getStatus().toUpperCase());
        }

        Reservation updated = reservationRepository.save(res);
        return new ApiResponse<>(1, "Reservation updated successfully", toDto(updated));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<ReservationDetailDTO> getReservationById(Integer reservationId) {
        Optional<Reservation> optionalRes = reservationRepository.findById(reservationId);
        if (optionalRes.isEmpty()) {
            return new ApiResponse<>(4, "Reservation not found", null);
        }

        Reservation res = optionalRes.get();
        RoomDTO room = fetchRoom(res.getRoomId());

        ReservationDetailDTO detailDTO = toDetailDto(res, room);
        return new ApiResponse<>(1, "Reservation retrieved successfully", detailDTO);
    }

    @Override
    public ApiResponse<Void> deleteReservation(Integer reservationId) {
        Optional<Reservation> optionalRes = reservationRepository.findById(reservationId);
        if (optionalRes.isEmpty()) {
            return new ApiResponse<>(4, "Reservation not found", null);
        }

        Reservation res = optionalRes.get();
        res.setStatus("CANCELLED");
        reservationRepository.save(res);
        return new ApiResponse<>(1, "Reservation cancelled successfully", null);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PageDTO<ReservationDetailDTO>> getReservations(int page, int size, String guestName, String status) {
        if (page < 0 || size <= 0 || size > 100) {
            return new ApiResponse<>(2, "Invalid pagination: page must be >= 0 and size between 1 and 100", null);
        }

        String filterName = (guestName != null && !guestName.isBlank()) ? guestName.trim() : null;
        String filterStatus = (status != null && !status.isBlank()) ? status.trim() : null;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("reservationId").ascending());
        Page<Reservation> resPage = reservationRepository.searchReservations(filterName, filterStatus, pageRequest);

        List<ReservationDetailDTO> detailList = resPage.getContent().stream()
                .map(r -> {
                    RoomDTO room = fetchRoom(r.getRoomId());
                    return toDetailDto(r, room);
                })
                .collect(Collectors.toList());

        PageDTO<ReservationDetailDTO> pageDTO = new PageDTO<>(
                detailList,
                resPage.getNumber(),
                resPage.getSize(),
                resPage.getTotalElements(),
                resPage.getTotalPages()
        );

        return new ApiResponse<>(1, "Reservations retrieved successfully", pageDTO);
    }

    private RoomDTO fetchRoom(Integer roomId) {
        try {
            ApiResponse<RoomDTO> resp = roomClient.getRoomById(roomId);
            if (resp != null && (resp.getStatus() == 1 || resp.getStatus() == 200) && resp.getData() != null) {
                return convertToRoomDTO(resp.getData());
            }
        } catch (FeignException.NotFound ex) {
            return null;
        } catch (Exception ex) {
            // Room service down or unreachable
            return null;
        }
        return null;
    }

    private RoomDTO convertToRoomDTO(Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof RoomDTO) {
            return (RoomDTO) data;
        }
        return objectMapper.convertValue(data, RoomDTO.class);
    }

    private ReservationDTO toDto(Reservation r) {
        return new ReservationDTO(
                r.getReservationId(),
                r.getRoomId(),
                r.getGuestName(),
                r.getGuestEmail(),
                r.getGuestPhone(),
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getNumberOfGuests(),
                r.getTotalAmount(),
                r.getStatus()
        );
    }

    private ReservationDetailDTO toDetailDto(Reservation r, RoomDTO room) {
        return new ReservationDetailDTO(
                r.getReservationId(),
                r.getRoomId(),
                r.getGuestName(),
                r.getGuestEmail(),
                r.getGuestPhone(),
                r.getCheckInDate(),
                r.getCheckOutDate(),
                r.getNumberOfGuests(),
                r.getTotalAmount(),
                r.getStatus(),
                room
        );
    }
}
