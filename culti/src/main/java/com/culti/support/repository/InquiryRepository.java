package com.culti.support.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.support.entity.Inquiry;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    // 특정 유저의 문의 내역을 최신순으로 조회
    List<Inquiry> findByUserIdOrderByCreatedAtDesc(Long userId);
}