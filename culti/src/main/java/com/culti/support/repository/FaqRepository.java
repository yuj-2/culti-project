package com.culti.support.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.culti.support.entity.Faq;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
	Page<Faq> findByFaqCategory(String category, Pageable pageable);
}