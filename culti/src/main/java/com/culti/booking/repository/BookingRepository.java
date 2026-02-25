package com.culti.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.culti.booking.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // 기본적인 저장(save), 조회(findById) 기능이 자동으로 포함됩니다.
}