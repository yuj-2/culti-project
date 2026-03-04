package com.culti.booking.entity;

import com.culti.content.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "schedule_seat")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule; // 어떤 상영 회차인지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat; // 어떤 좌석인지

    @Column(nullable = false, length = 20)
    private String status; // AVAILABLE(가능), OCCUPIED(완료) 등
}