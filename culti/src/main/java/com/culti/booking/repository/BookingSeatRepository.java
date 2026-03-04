package com.culti.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.booking.entity.BookingSeat;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    List<BookingSeat> findByBookingBookingId(Long bookingId);

}