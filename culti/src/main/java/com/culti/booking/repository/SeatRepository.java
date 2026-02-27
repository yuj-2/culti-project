package com.culti.booking.repository;

import com.culti.booking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    // 예매 시 좌석 이름(A1, B2 등)으로 Seat 엔티티를 찾아야 한다면 추가
    // Optional<Seat> findBySeatRowAndSeatCol(String seatRow, Integer seatCol);
}