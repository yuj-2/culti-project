package com.culti.auth.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.culti.auth.repository.EmailVerificationRepository;
import com.culti.auth.repository.SocialAuthRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class SocialAuthServiceImpl implements SocialAuthService{
	
	private final SocialAuthRepository socialAuthRepository;
	
	@Override
	public boolean existsByUser_UserIdAndProvider(Long userId, String provider) {
		// TODO Auto-generated method stub
		boolean result= this.socialAuthRepository.existsByUser_UserIdAndProvider(userId, provider);
		return result;
	}
	
}
