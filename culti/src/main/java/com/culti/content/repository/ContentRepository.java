package com.culti.content.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.content.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Long> {

	List<Content> findByCategoryAndTitleContainingIgnoreCase(String category, String title, Sort sort);
	
}
