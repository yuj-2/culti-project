package com.culti.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{

    private final JavaMailSender emailSender; // 스프링이 자동으로 주입해줍니다.
    
    @Override
    public void sendSimpleEmail(String toEmail) {
        // 1. 6자리 인증번호 생성 (테스트용)
        String authCode = String.valueOf((int)(Math.random() * 899999) + 100000);

        // 2. 메시지 구성
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("sykimhoyeon@gmail.com"); // 보내는 사람 (생략 가능하지만 명시 권장)
        message.setTo(toEmail);               // 받는 사람
        message.setSubject("인증번호 테스트입니다.");
        message.setText("인증번호: " + authCode);

        // 3. 발송
        emailSender.send(message);
        
        System.out.println("메일 발송 완료! 인증번호: " + authCode);
    }
}
