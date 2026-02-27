package com.culti.content.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.content.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findByContentId(Long contentId, Pageable pageable);
	
	List<Review> findTop3ByContent_IdOrderByRatingDescCreatedAtDesc(Long contentId);
	
}
