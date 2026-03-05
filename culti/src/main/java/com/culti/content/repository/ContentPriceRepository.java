package com.culti.content.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.content.entity.ContentPrice;

public interface ContentPriceRepository extends JpaRepository<ContentPrice, Long> {
	List<ContentPrice> findByContentId(Long contentId);
}
