package com.culti.auth.controller;

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
    public String sendEmail(@RequestBody String email) {
        // 1. 이메일 발송 로직
		this.emailService.sendSimpleEmail(email);
        // 2. 인증번호 저장 (유효시간 설정)
        return "인증번호가 발송되었습니다.";
    }

	/*
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyCode(@RequestBody VerifyRequest request) {
        // 1. 저장된 번호와 비교
        // 2. 일치 여부 반환
        return ResponseEntity.ok(true);
    }*/
}
