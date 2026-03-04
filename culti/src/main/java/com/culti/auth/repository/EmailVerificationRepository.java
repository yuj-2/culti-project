package com.culti.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.auth.entity.EmailVerification;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long>{
	// 이메일로 검색 후, ID 내림차순으로 정렬하여 가장 첫 번째(최근) 데이터 하나만 조회
    Optional<EmailVerification> findFirstByEmailOrderByIdDesc(String email);
}
