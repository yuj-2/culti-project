package com.culti.booking.service;

import java.util.Map;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
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
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;

    /**
     * 결제 정보 검증 및 DB 저장 프로세스
     * @return 생성된 Booking의 ID (결과 페이지 리다이렉트용)
     */
    @Transactional
    public Long processPayment(Map<String, Object> paymentData, String loginEmail) {
        // 1. 유저 정보 조회
        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 스케줄 정보 조회 (payment.js의 scheduleId와 매칭)
        // 만약 JS에서 'scheduleId'로 보낸다면 키값을 맞춰야 합니다.
        Object scheduleIdObj = paymentData.get("scheduleId") != null ? 
                               paymentData.get("scheduleId") : paymentData.get("content_id");
                               
        Long scheduleId = Long.parseLong(scheduleIdObj.toString());
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("상영 스케줄을 찾을 수 없습니다."));

        // 3. 결제 데이터 추출
        Integer amount = Integer.parseInt(paymentData.get("total_price").toString());
        String merchantUid = (String) paymentData.get("merchant_uid");
        String impUid = (String) paymentData.get("imp_uid");
        String category = (String) paymentData.get("category");
        
        // 좌석 ID 리스트 (예: "101,102")
        String seatIdsStr = (String) paymentData.get("seatIds"); 
        if (seatIdsStr == null) seatIdsStr = (String) paymentData.get("seat_info");
        
        String[] seatIdArray = seatIdsStr.split(",");

        // 4. Booking 엔티티 생성
        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                .merchantUid(merchantUid)
                .impUid(impUid)
                .category(category)
                .totalPrice(amount)
                .status("PAID")
                .paymentStatus("COMPLETED")
                .paymentMethod("CARD")
                .ticketCount(seatIdArray.length)
                .discountAmount(0)
                .bookingSeats(new ArrayList<>()) // 리스트 초기화
                .build();

        // 5. 개별 좌석(BookingSeat) 저장 로직
        for (String seatId : seatIdArray) {
            Seat seat = seatRepository.findById(Long.parseLong(seatId.trim()))
                    .orElseThrow(() -> new RuntimeException("좌석 정보를 찾을 수 없습니다."));

            BookingSeat bookingSeat = BookingSeat.builder()
                    .seat(seat)
                    .booking(booking)
                    .build();
            
            // Cascade 설정에 따라 같이 저장되도록 리스트에 추가
            booking.getBookingSeats().add(bookingSeat);
        }

        // [핵심 수정] 저장 후 생성된 엔티티를 받아서 ID를 반환합니다.
        Booking savedBooking = bookingRepository.save(booking);
        
        return savedBooking.getBookingId(); // 컨트롤러의 Type mismatch 에러 해결!
    }
}