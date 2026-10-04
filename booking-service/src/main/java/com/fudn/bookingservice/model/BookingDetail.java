package com.fudn.bookingservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_detail")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String showtimeId;
    private String movieId;
    private String movieTitle;
    private String roomName;
    private String seatCode;
    private BigDecimal ticketPrice;
}
