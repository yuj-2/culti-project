package com.culti.booking.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.culti.booking.service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAndSave(@RequestBody Map<String, Object> paymentData, 
                                          @AuthenticationPrincipal UserDetails user) {
        // 세션에서 로그인한 사용자의 이메일을 가져옵니다.
        String loginEmail = user.getUsername();
        
        // 결제 데이터와 유저 이메일을 서비스로 넘겨 DB에 저장합니다.
        paymentService.processPayment(paymentData, loginEmail);
        
        return ResponseEntity.ok("SUCCESS");
    }
}