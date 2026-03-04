package com.culti.content.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.content.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Long> {

	Page<Content> findByCategoryAndTitleContainingIgnoreCase(String category, String keyword, Pageable pageable);

	List<Content> findByCategory(String category);
	
}
