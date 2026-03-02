package com.culti.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.auth.entity.Terms;

public interface TermsRepository extends JpaRepository<Terms, Long>{
	// 현재 활성화된(사용 중인) 약관 리스트만 가져오기
    List<Terms> findAllByIsActive(String isActive);
}
