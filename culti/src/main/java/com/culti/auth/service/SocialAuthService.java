package com.culti.auth.service;

public interface SocialAuthService {
	
	//특정 유저가 현재 소셜 로그인 데이터가 저장이 되어있는지 확인
	boolean existsByUser_UserIdAndProvider(Long userId, String provider);
}
