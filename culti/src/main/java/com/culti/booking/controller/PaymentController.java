package com.culti.booking.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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
            @RequestBody BookingRequestDTO requestDTO,
            Principal principal
    ) {

        String email = principal.getName();

        Long bookingId = paymentService.processPayment(
                requestDTO,
                email,
                requestDTO.getImpUid(),
                requestDTO.getMerchantUid()
        );

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "bookingId", bookingId
        ));
    }
}