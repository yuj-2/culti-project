package com.culti.content.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.culti.content.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findByContentId(Long contentId, Pageable pageable);
	
	@Query("SELECT r FROM Review r WHERE r.content.id = :contentId AND r.photoUrls IS NOT NULL AND r.photoUrls != ''")
    Page<Review> findByContentIdWithPhotos(@Param("contentId") Long contentId, Pageable pageable);
	
}
