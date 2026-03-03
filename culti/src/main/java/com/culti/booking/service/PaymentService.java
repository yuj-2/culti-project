package com.culti.booking.service;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.entity.Booking;
import com.culti.booking.entity.BookingSeat;
import com.culti.booking.entity.ScheduleSeat;
import com.culti.booking.entity.Seat;
import com.culti.booking.repository.BookingRepository;
import com.culti.booking.repository.ScheduleRepository;
import com.culti.booking.repository.ScheduleSeatRepository;
import com.culti.booking.repository.SeatRepository;
import com.culti.content.entity.Schedule;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.api-secret}")
    private String apiSecret;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public Long processPayment(
            BookingRequestDTO requestDTO,
            String loginEmail,
            String impUid,
            String merchantUid
    ) {

        // 1️⃣ 기본 검증
        validateRequest(requestDTO);

        // 2️⃣ 서버 금액 재계산 (✅ 4종 인원 기준)
        int serverAmount = calculateTotalAmount(requestDTO);

        System.out.println("클라이언트 금액: " + requestDTO.getTotalPrice());
        System.out.println("서버 계산 금액: " + serverAmount);

        if (!serverAmountEquals(requestDTO.getTotalPrice(), serverAmount)) {
            throw new IllegalArgumentException("금액 위변조 감지");
        }

        // 3️⃣ 포트원 결제 검증 (impUid/merchantUid/amount)
        verifyPortOnePayment(
                impUid,
                merchantUid,
                serverAmount
        );

        // 4️⃣ 사용자 & 스케줄 조회
        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        Schedule schedule = scheduleRepository.findById(requestDTO.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("스케줄 없음"));

        // 5️⃣ Booking 생성
        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                .bookingNumber(requestDTO.getMerchantUid())
                .merchantUid(requestDTO.getMerchantUid())
                .impUid(requestDTO.getImpUid())
                .totalPrice(serverAmount)
                .status("PAID")
                .paymentMethod("KAKAOPAY")
                .ticketCount(requestDTO.getSeatIds().size())
                .discountAmount(0)
                .bookingSeats(new ArrayList<>())
                .build();

        // 6️⃣ 좌석 처리
        for (String seatStr : requestDTO.getSeatIds()) {

            String row = seatStr.substring(0, 1);
            int col = Integer.parseInt(seatStr.substring(1));

            Seat seat = seatRepository
                    .findByPlaceIdAndSeatRowAndSeatCol(
                            schedule.getPlace().getPlaceId(),
                            row,
                            col
                    )
                    .orElseThrow(() -> new IllegalArgumentException("좌석 없음: " + seatStr));

            ScheduleSeat scheduleSeat = scheduleSeatRepository
                    .findByScheduleAndSeatForUpdate(schedule.getScheduleId(), seat.getSeatId())
                    .orElseThrow(() -> new IllegalArgumentException("schedule_seat 없음"));

            if (!"AVAILABLE".equals(scheduleSeat.getStatus())) {
                throw new IllegalStateException("이미 예약된 좌석: " + seatStr);
            }

            scheduleSeat.setStatus("BOOKED");

            BookingSeat bookingSeat = BookingSeat.builder()
                    .scheduleSeat(scheduleSeat)
                    .booking(booking)
                    .build();

            booking.getBookingSeats().add(bookingSeat);
        }

        return bookingRepository.save(booking).getBookingId();
    }

    // ===============================
    // 🔐 포트원 결제 검증
    // ===============================
 
    private void verifyPortOnePayment(String impUid, String merchantUid, int expectedAmount) {

        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = "https://api.iamport.kr/payments/" + impUid;

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map outer = response.getBody();
        if (outer == null || outer.get("response") == null) {
            throw new IllegalStateException("포트원 결제 조회 실패");
        }

        Map body = (Map) outer.get("response");

        String status = (String) body.get("status");
        String portoneMerchantUid = (String) body.get("merchant_uid");
        int amount = ((Number) body.get("amount")).intValue();

        if (!"paid".equals(status)) {
            throw new IllegalStateException("결제 완료 상태가 아님");
        }

        if (!merchantUid.equals(portoneMerchantUid)) {
            throw new IllegalArgumentException("merchantUid 불일치");
        }

        if (amount != expectedAmount) {
            throw new IllegalArgumentException("포트원 금액 불일치");
        }
    }
    private String getAccessToken() {

        Map<String, String> tokenRequest = Map.of(
                "imp_key", apiKey,
                "imp_secret", apiSecret
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        "https://api.iamport.kr/users/getToken",
                        tokenRequest,
                        Map.class
                );

        Map body = (Map) response.getBody().get("response");
        if (body == null) throw new IllegalStateException("포트원 토큰 발급 실패");

        return (String) body.get("access_token");
    }

    // ===============================
    // 🧮 서버 금액 재계산 (✅ 4종 인원 기준)
    // ===============================

    private int calculateTotalAmount(BookingRequestDTO dto) {

        // ⚠️ 일단은 프론트와 동일하게 고정가로 맞추기 (DB 좌석 base_price와 무관)
        final int ADULT_PRICE = 15000;
        final int YOUTH_PRICE = 12000;
        final int SENIOR_PRICE = 10000;
        final int SPECIAL_PRICE = 8000;

        int adult = dto.getAdultCount() == null ? 0 : dto.getAdultCount();
        int youth = dto.getYouthCount() == null ? 0 : dto.getYouthCount();
        int senior = dto.getSeniorCount() == null ? 0 : dto.getSeniorCount();
        int special = dto.getSpecialCount() == null ? 0 : dto.getSpecialCount();

        return adult * ADULT_PRICE
                + youth * YOUTH_PRICE
                + senior * SENIOR_PRICE
                + special * SPECIAL_PRICE;
    }

    private void validateRequest(BookingRequestDTO dto) {

        if (dto == null) throw new IllegalArgumentException("요청 없음");
        if (dto.getScheduleId() == null) throw new IllegalArgumentException("scheduleId 누락");
        if (dto.getSeatIds() == null || dto.getSeatIds().isEmpty())
            throw new IllegalArgumentException("좌석 없음");
        if (dto.getImpUid() == null) throw new IllegalArgumentException("impUid 누락");
        if (dto.getMerchantUid() == null) throw new IllegalArgumentException("merchantUid 누락");
        if (dto.getTotalPrice() == null) throw new IllegalArgumentException("totalPrice 누락");

        // ✅ 인원 4종 중 하나라도 있어야 함 (전부 null/0이면 결제 불가)
        int adult = dto.getAdultCount() == null ? 0 : dto.getAdultCount();
        int youth = dto.getYouthCount() == null ? 0 : dto.getYouthCount();
        int senior = dto.getSeniorCount() == null ? 0 : dto.getSeniorCount();
        int special = dto.getSpecialCount() == null ? 0 : dto.getSpecialCount();

        int totalCount = adult + youth + senior + special;

        if (totalCount <= 0) {
            throw new IllegalArgumentException("인원 정보 없음");
        }

        // ✅ 좌석 수와 인원 수가 다르면 막기
        if (dto.getSeatIds().size() != totalCount) {
            throw new IllegalArgumentException("좌석 수와 인원 수가 일치하지 않습니다.");
        }
    }

    private boolean serverAmountEquals(Integer client, int server) {
        return client != null && client == server;
    }
}