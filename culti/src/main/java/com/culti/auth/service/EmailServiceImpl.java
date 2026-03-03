package com.culti.auth.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.culti.auth.entity.EmailVerification;
import com.culti.auth.repository.EmailVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService{
	
	private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender emailSender;

    
    @Override
    public String sendSimpleEmail(String toEmail) {
    	try {
            String authCode = String.valueOf((int)(Math.random() * 899999) + 100000);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("sykimhoyeon@gmail.com");
            message.setTo(toEmail);
            message.setSubject("인증번호 테스트입니다.");
            message.setText("인증번호: " + authCode);

            emailSender.send(message);
            
            //System.out.println("메일 발송 성공: " + authCode);
            
            
            
            return authCode;

        } catch (MailException e) {
            // 발송 실패 시 실행되는 구역
            System.err.println("메일 발송 중 오류 발생: " + e.getMessage());
            // 여기서 로그를 남기거나, 사용자에게 실패 메시지를 던지는 처리를 합니다.
           throw new RuntimeException("이메일 발송에 실패했습니다. 주소를 확인해주세요.");
        }
    }

	@Override
	public void insertEmailVerification(String email,String authCode) {
		
		System.out.println(email);
		
		// TODO Auto-generated method stub
		EmailVerification emailVerification=EmailVerification.builder()
        		.email(email)
        		.authCode(authCode)
        		.purpose("회원인증")
        		.expiresAt(LocalDateTime.now().plusMinutes(3))
        		.build();
		
		this.emailVerificationRepository.save(emailVerification);
		
	}

	@Override
	public String returnAuthCode(String email) {
		
		Optional<EmailVerification> result=this.emailVerificationRepository.findFirstByEmailOrderByIdDesc(email);
		
	    if (result.isPresent()) {
	    	System.out.println("이메일 리턴 메서드 진입");
	    	
	        EmailVerification emailVerification = result.get();
	        
	        return emailVerification.getAuthCode();
	    }
		
		
		return null;
	}
}
