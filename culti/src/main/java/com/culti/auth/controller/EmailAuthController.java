package com.culti.auth.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.culti.auth.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class EmailAuthController {
	
	private final EmailService emailService;
	
	
	@PostMapping("/send")
    public String sendEmail(@RequestBody Map<String, String> payload) {
		String email = payload.get("email");
		
        // 1. 이메일 발송 로직
		String authCode=this.emailService.sendSimpleEmail(email);
		
		//2. 이메일 인증 테이블에 인증번호 저장
		this.emailService.insertEmailVerification(email,authCode);
		
        // 3. 인증번호 저장 (유효시간 설정)
        return "인증번호가 발송되었습니다.";
    }

	
    @PostMapping("/verify")
    public boolean verifyCode(@RequestBody Map<String, String> payload) {
    	String email = payload.get("email");
        String inputAuthCode = payload.get("inputAuthCode");
        
    	String authCode=this.emailService.returnAuthCode(email);
    	
    	if (authCode.equals(inputAuthCode)) {
			return true;
		}
    	
        
        return false;
    }
}
