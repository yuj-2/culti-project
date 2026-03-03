package com.culti.booking.service;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.entity.Booking;
import com.culti.booking.entity.BookingSeat;
import com.culti.booking.entity.Seat;
import com.culti.booking.repository.BookingRepository;
import com.culti.booking.repository.ScheduleRepository;
import com.culti.booking.repository.SeatRepository;
import com.culti.content.entity.Schedule;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    @Value("${portone.v2.api-secret}")
    private String v2ApiSecret;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public Long processPayment(
            BookingRequestDTO requestDTO,
            String loginEmail,
            String impUid,
            String merchantUid
    ) {

        if (impUid == null || impUid.isBlank()) {
            throw new RuntimeException("impUid 누락");
        }

        // 🔥 V2 결제 검증
        validatePaymentWithPortOne(
                impUid,
                requestDTO.getTotalPrice()
        );

        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        Schedule schedule = scheduleRepository.findById(
                requestDTO.getScheduleId()
        ).orElseThrow(() -> new RuntimeException("스케줄 없음"));

        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                .bookingNumber("B" + System.currentTimeMillis())
                .merchantUid(merchantUid)
                .impUid(impUid)
                .totalPrice(requestDTO.getTotalPrice())
                .status("PAID")
                .paymentStatus("COMPLETED")
                .paymentMethod("KAKAOPAY")
                .ticketCount(requestDTO.getSeatIds().size())
                .discountAmount(0)
                .category(schedule.getContent().getCategory())
                .bookingSeats(new ArrayList<>())
                .build();

        for (String seatStr : requestDTO.getSeatIds()) {

            String row = seatStr.substring(0, 1);
            int col = Integer.parseInt(seatStr.substring(1));

            Seat seat = seatRepository.findByPlaceIdAndSeatRowAndSeatCol(
                    schedule.getPlace().getPlaceId(),
                    row,
                    col
            ).orElseThrow(() ->
                    new RuntimeException("좌석 정보를 찾을 수 없습니다: " + seatStr)
            );

            BookingSeat bookingSeat = BookingSeat.builder()
                    .seat(seat)
                    .booking(booking)
                    .build();

            booking.getBookingSeats().add(bookingSeat);
        }

        Booking saved = bookingRepository.save(booking);

        return saved.getBookingId();
    }

    /**
     * 🔥 PortOne V2 결제 검증
     */
    private void validatePaymentWithPortOne(
            String impUid,
            Integer expectedAmount
    ) {

        String url = "https://api.portone.io/payments/" + impUid;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + v2ApiSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("포트원 조회 실패");
        }

        Map body = response.getBody();

        if (body == null) {
            throw new RuntimeException("결제 응답 없음");
        }

        // 🔥 여기 수정
        Map amountMap = (Map) body.get("amount");

        if (amountMap == null) {
            throw new RuntimeException("금액 정보 없음");
        }

        Number totalAmount = (Number) amountMap.get("total");

        int actualAmount = totalAmount.intValue();

        if (expectedAmount == null || expectedAmount != actualAmount) {
            throw new RuntimeException("금액 위변조 감지");
        }
    }
}