package com.culti.booking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.culti.booking.entity.ScheduleSeat;

import jakarta.persistence.LockModeType;

@Repository
public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long> {

    /**
     * 🔐 특정 회차 + 특정 좌석 조회 (동시성 방지용 Pessimistic Lock)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ss from ScheduleSeat ss
        where ss.schedule.scheduleId = :scheduleId
          and ss.seat.seatId = :seatId
    """)
    Optional<ScheduleSeat> findByScheduleAndSeatForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("seatId") Long seatId
    );

    /**
     * 특정 회차의 전체 좌석 조회
     */
    List<ScheduleSeat> findBySchedule_ScheduleId(Long scheduleId);

    /**
     * 특정 회차 + 상태 조회 (예: AVAILABLE 좌석만)
     */
    List<ScheduleSeat> findBySchedule_ScheduleIdAndStatus(
            Long scheduleId,
            String status
    );

    /**
     * 특정 회차의 좌석 존재 여부 확인
     */
    boolean existsBySchedule_ScheduleIdAndSeat_SeatId(
            Long scheduleId,
            Long seatId
    );
}