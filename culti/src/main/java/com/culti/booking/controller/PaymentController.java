package com.culti.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.culti.booking.entity.Booking;
import com.culti.booking.repository.BookingRepository;
import com.culti.booking.service.BookingService;
import com.culti.booking.service.PaymentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    /**
     * 토스 결제 성공 시 호출되는 콜백 URL
     */
    @GetMapping("/success")
    public String success(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId") String orderId,
            @RequestParam("amount") Long amount
    ) throws Exception {
        
        // 1. 토스 API 최종 승인 (PaymentService 내부에서 결제 승인 요청)
        // [참고] PaymentService 내에서도 findByBookingNumber를 사용하도록 수정했는지 확인하세요.
        paymentService.confirmPayment(paymentKey, orderId, amount);
        
        // 2. 예매 상태 확정 (PENDING -> PAID 및 좌석 OCCUPIED 처리)
        // [참고] BookingService에 이미 만들어둔 메서드를 호출합니다.
        bookingService.confirmBookingStatus(orderId);
        
        // 3. 결과 페이지(/reservation/booking/result/{id})로 가기 위해 PK인 bookingId를 찾습니다.
        // [수정] findByBookingNumber 메서드명이 리포지토리와 일치해야 합니다.
        Booking booking = bookingRepository.findByBookingNumber(orderId)
                .orElseThrow(() -> new RuntimeException("예매 내역을 찾을 수 없습니다. 주문번호: " + orderId));
        
        // 4. 최종 결과 화면으로 리다이렉트
        return "redirect:/reservation/booking/result/" + booking.getBookingId(); 
    }
    
    /**
     * 결제 실패 또는 사용자가 취소했을 때 호출
     */
    @GetMapping("/fail")
    public String fail() {
        // [팁] 실패 페이지(booking_fail.html)를 reservation 폴더 안에 만드시면 됩니다.
        return "reservation/booking_fail"; 
    }
}