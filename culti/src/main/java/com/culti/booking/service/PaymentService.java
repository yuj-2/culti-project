package com.culti.booking.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.culti.booking.entity.Booking;
import com.culti.booking.repository.BookingRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final BookingRepository bookingRepository;
    // schedule_seat 테이블 수정을 위해 필요
    // private final ScheduleSeatRepository scheduleSeatRepository; 

    @Value("${toss.secret.key}")
    private String secretKey;

    @Transactional
    public void confirmPayment(String paymentKey, String orderId, Long amount) throws Exception {
        String authorizations = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + authorizations);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        JSONObject obj = new JSONObject();
        obj.put("orderId", orderId);
        obj.put("amount", amount);
        obj.put("paymentKey", paymentKey);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(obj.toString().getBytes("utf-8"));
        }

        // [해결] responseCode 변수를 여기서 선언하고 할당합니다.
        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            // 1. booking 테이블에서 해당 예매 정보 찾기
            Booking booking = bookingRepository.findByBookingNumber(orderId)
                .orElseThrow(() -> new RuntimeException("예매 정보를 찾을 수 없습니다."));
            
            // 2. 예매 상태 업데이트 (PENDING -> PAID)
            booking.setStatus("PAID");
            booking.setPaymentMethod("CARD"); // 토스에서 받은 결제수단으로 동적 할당 가능
            
            // 3. 영화 예매라면 좌석 상태도 'AVAILABLE'에서 'OCCUPIED'로 바꿔야 함
            // 예: scheduleSeatService.updateStatusByBooking(booking.getBookingId(), "OCCUPIED");
            
            System.out.println("결제 및 예매 확정 완료: " + orderId);
        } else {
            // 실패 시 에러 처리
            throw new RuntimeException("토스 결제 승인 실패. 코드: " + responseCode);
        }
    }
}