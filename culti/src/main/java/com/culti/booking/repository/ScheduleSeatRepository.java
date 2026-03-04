package com.culti.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import com.culti.booking.entity.ScheduleSeat;

public interface ScheduleSeatRepository extends JpaRepository<ScheduleSeat, Long> {

    List<ScheduleSeat> findBySchedule_ScheduleId(Long scheduleId);

    boolean existsBySchedule_ScheduleIdAndSeat_SeatIdInAndStatus(
            Long scheduleId,
            List<Long> seatIds,
            String status
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE ScheduleSeat ss
        SET ss.status = 'OCCUPIED'
        WHERE ss.schedule.scheduleId = :scheduleId
        AND ss.seat.seatId IN :seatIds
    """)
    void updateStatusToOccupied(
            @Param("scheduleId") Long scheduleId,
            @Param("seatIds") List<Long> seatIds
    );
    
    @Modifying
    @Transactional
    @Query("""
    UPDATE ScheduleSeat s
    SET s.status = 'AVAILABLE'
    WHERE s.schedule.scheduleId = :scheduleId
    AND s.seat.seatId IN :seatIds
    """)
    void updateStatusToAvailable(
            @Param("scheduleId") Long scheduleId,
            @Param("seatIds") List<Long> seatIds
    );
    
    @Query("""
    	    SELECT ss
    	    FROM ScheduleSeat ss
    	    JOIN FETCH ss.seat s
    	    WHERE ss.schedule.scheduleId = :scheduleId
    	""")
    	List<ScheduleSeat> findAllWithSeatByScheduleId(@Param("scheduleId") Long scheduleId);
}