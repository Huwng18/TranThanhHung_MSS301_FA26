package com.fudn.bookingservice.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
@Data
@Builder
public class RevenueByMovie {
    private String movieId;
    private String movieTitle;
    private int ticketsSold;
    private BigDecimal revenue;
}
