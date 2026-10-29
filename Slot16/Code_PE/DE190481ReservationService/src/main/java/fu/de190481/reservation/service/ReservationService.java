package fu.de190481.reservation.service;

import fu.de190481.reservation.dto.ApiResponse;
import fu.de190481.reservation.dto.PageDTO;
import fu.de190481.reservation.dto.ReservationDTO;
import fu.de190481.reservation.dto.ReservationDetailDTO;

public interface ReservationService {

    ApiResponse<ReservationDTO> createReservation(ReservationDTO reservationDTO);

    ApiResponse<ReservationDTO> updateReservation(Integer reservationId, ReservationDTO reservationDTO);

    ApiResponse<ReservationDetailDTO> getReservationById(Integer reservationId);

    ApiResponse<Void> deleteReservation(Integer reservationId);

    ApiResponse<PageDTO<ReservationDetailDTO>> getReservations(int page, int size, String guestName, String status);
}
