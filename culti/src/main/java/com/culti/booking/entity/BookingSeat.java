package com.culti.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter 
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookingseat")
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingSeatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat; 
}