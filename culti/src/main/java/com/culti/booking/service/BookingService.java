package com.culti.booking.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.dto.BookingResponseDTO;
import com.culti.booking.entity.Booking;
import com.culti.booking.entity.BookingSeat;
import com.culti.booking.entity.ScheduleSeat;
import com.culti.booking.entity.Seat;
import com.culti.booking.repository.BookingRepository;
import com.culti.booking.repository.ScheduleRepository;
import com.culti.booking.repository.ScheduleSeatRepository;
import com.culti.content.entity.Schedule;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;

    @Value("${portone.api.key}")
    private String portoneApiKey;

    @Value("${portone.api.secret}")
    private String portoneApiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public Long createBooking(BookingRequestDTO requestDTO, String email) {

        if (requestDTO.getScheduleId() == null)
            throw new IllegalArgumentException("scheduleId 누락");

        if (requestDTO.getSeatIds() == null || requestDTO.getSeatIds().isEmpty())
            throw new IllegalArgumentException("seatIds 누락");

        if (requestDTO.getImpUid() == null)
            throw new IllegalArgumentException("impUid 누락");

        if (requestDTO.getMerchantUid() == null)
            throw new IllegalArgumentException("merchantUid 누락");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Schedule schedule = scheduleRepository.findById(requestDTO.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("회차 없음"));

        // 🔐 좌석 잠금 조회
        List<ScheduleSeat> scheduleSeats = requestDTO.getSeatIds().stream()
                .map(Long::parseLong)
                .map(seatId -> scheduleSeatRepository
                        .findByScheduleAndSeatForUpdate(schedule.getScheduleId(), seatId)
                        .orElseThrow(() -> new IllegalArgumentException("해당 회차 좌석 없음")))
                .toList();

        // 상태 확인
        for (ScheduleSeat ss : scheduleSeats) {
            if (!"AVAILABLE".equals(ss.getStatus())) {
                throw new IllegalStateException("이미 예약된 좌석 포함");
            }
        }

        // 서버 금액 계산
        int serverTotal = scheduleSeats.stream()
                .mapToInt(ss -> ss.getSeat().getBasePrice())
                .sum();

        if (serverTotal != requestDTO.getTotalPrice()) {
            throw new IllegalArgumentException("금액 위변조 감지");
        }

        // PortOne 검증
        PortOnePayment payment = verifyPortOnePayment(
                requestDTO.getImpUid(),
                requestDTO.getMerchantUid()
        );

        if (payment.amount != serverTotal)
            throw new IllegalArgumentException("포트원 금액 불일치");

        if (!"paid".equalsIgnoreCase(payment.status))
            throw new IllegalStateException("결제 완료 상태 아님");

        // Booking 생성
        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                .bookingNumber(requestDTO.getMerchantUid())
                .totalPrice(serverTotal)
                .status("PAID")
                .createdAt(LocalDateTime.now())
                .ticketCount(scheduleSeats.size())
                .discountAmount(0)
                .paymentMethod("CARD")
                .build();

        // 좌석 매핑
        for (ScheduleSeat ss : scheduleSeats) {

            ss.setStatus("BOOKED");

            BookingSeat bookingSeat = BookingSeat.builder()
                    .booking(booking)
                    .scheduleSeat(ss)
                    .build();

            booking.getBookingSeats().add(bookingSeat);
        }

        return bookingRepository.save(booking).getBookingId();
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingResult(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("예매 없음"));

        List<String> seatNames = booking.getBookingSeats().stream()
                .map(bs -> {
                    Seat seat = bs.getScheduleSeat().getSeat();
                    return seat.getSeatRow() + seat.getSeatCol();
                })
                .collect(Collectors.toList());

        return BookingResponseDTO.builder()
                .bookingNumber(booking.getBookingNumber())
                .movieTitle(booking.getSchedule().getContent().getTitle())
                .showTime(String.valueOf(booking.getSchedule().getStartTime()))
                .totalPrice(booking.getTotalPrice())
                .seatNames(seatNames)
                .build();
    }

    private PortOnePayment verifyPortOnePayment(String impUid, String expectedMerchantUid) {

        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> responseEntity = restTemplate.exchange(
                "https://api.iamport.kr/payments/" + impUid,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map body = responseEntity.getBody();
        Map response = (Map) body.get("response");

        String merchantUid = String.valueOf(response.get("merchant_uid"));
        String status = String.valueOf(response.get("status"));
        int amount = ((Number) response.get("amount")).intValue();

        if (!expectedMerchantUid.equals(merchantUid))
            throw new IllegalArgumentException("merchantUid 불일치");

        return new PortOnePayment(status, amount);
    }

    private String getAccessToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "imp_key", portoneApiKey,
                "imp_secret", portoneApiSecret
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        Map res = restTemplate.postForObject(
                "https://api.iamport.kr/users/getToken",
                entity,
                Map.class
        );

        Map response = (Map) res.get("response");
        return String.valueOf(response.get("access_token"));
    }

    private static class PortOnePayment {
        private final String status;
        private final int amount;

        public PortOnePayment(String status, int amount) {
            this.status = status;
            this.amount = amount;
        }
    }
}