package com.culti.booking.entity;

import com.culti.content.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "schedule_seat",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_schedule_seat", columnNames = {"schedule_id", "seat_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_seat_id")
    private Long scheduleSeatId;

    // 회차 (schedule_id FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    // 좌석 (seat_id FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    // 좌석 상태 (AVAILABLE / BOOKED 등)
    @Column(nullable = false, length = 20)
    private String status;
}