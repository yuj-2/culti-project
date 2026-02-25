package com.culti.support.repository;

import com.culti.support.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    // 기본 findAll() 메서드를 사용합니다.
}