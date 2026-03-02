package com.culti.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.auth.entity.SocialAuth;
import com.culti.auth.entity.User;

public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long>{
	
	// 특정 유저가 특정 소셜 업체(provider)로 이미 연동되어 있는지 확인
	boolean existsByUserAndProvider(User user, String provider);
}
