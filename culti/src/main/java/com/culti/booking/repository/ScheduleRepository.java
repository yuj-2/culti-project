package com.culti.booking.repository;

import com.culti.content.entity.Content;
import com.culti.content.entity.Schedule;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

	List<Schedule> findByShowTimeGreaterThanEqualAndShowTimeLessThan(LocalDateTime start, LocalDateTime end);
	

	// 추가
	void deleteAllByContent(Content content);
	

}