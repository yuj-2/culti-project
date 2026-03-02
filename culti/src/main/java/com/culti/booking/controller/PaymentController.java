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
        // 세션 정보 확인
        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        String loginEmail = user.getUsername();
        
        // [수정] 결제 정보를 처리하고 생성된 bookingId를 반환받습니다.
        Long bookingId = paymentService.processPayment(paymentData, loginEmail);
        
        // [수정] JSON 형태로 전송하여 payment.js가 리다이렉트 할 수 있게 합니다.
        return ResponseEntity.ok(Map.of("status", "SUCCESS", "bookingId", bookingId));
    }
}