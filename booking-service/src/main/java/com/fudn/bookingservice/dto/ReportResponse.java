package com.fudn.bookingservice.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
@Data
@Builder
public class ReportResponse {
    private int totalBookings;
    private int totalTickets;
    private BigDecimal totalRevenue;
    private List<RevenueByMovie> revenueByMovie;
}
