package com.culti.booking.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.culti.booking.entity.Seat;

	@Repository
	public interface SeatRepository extends JpaRepository<Seat, Long> {
	    // SQL: seat_row(VARCHAR), seat_col(INT)와 매칭
	    @Query("SELECT s FROM Seat s WHERE s.place.placeId = :placeId AND s.seatRow = :seatRow AND s.seatCol = :seatCol")
	    Optional<Seat> findByPlaceIdAndSeatRowAndSeatCol(
	        @Param("placeId") Long placeId, 
	        @Param("seatRow") String seatRow, 
	        @Param("seatCol") int seatCol
	    );
	}