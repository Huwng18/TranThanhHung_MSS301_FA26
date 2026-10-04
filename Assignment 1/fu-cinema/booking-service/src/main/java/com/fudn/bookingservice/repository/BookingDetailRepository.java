package com.fudn.bookingservice.repository;

import com.fudn.bookingservice.model.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {
    List<BookingDetail> findByShowtimeId(String showtimeId);
}
