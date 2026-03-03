package com.culti.auth.service;

public interface EmailService {
	
	//메일 보내기
	String sendSimpleEmail(String toEmail);
	
	//이메일 인증 테이블에 값 넣기
	void insertEmailVerification(String email,String authCode);
}
