package com.fudn.bookingservice.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class BookingResponse {
    private Long bookingId;
    private Long customerId;
    private LocalDateTime bookingDate;
    private BigDecimal totalPrice;
    private String bookingStatus;
    private List<BookingDetailResponse> details;
}
