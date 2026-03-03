package com.culti.booking.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAndSave(
            @RequestBody BookingRequestDTO dto,
            @AuthenticationPrincipal UserDetails user) {

        if (user == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        Long bookingId = paymentService.processPayment(
                dto,
                user.getUsername(),
                dto.getImpUid(),
                dto.getMerchantUid()
        );

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "bookingId", bookingId
        ));
    }
  }
