package com.culti.booking.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.auth.entity.User; //
import com.culti.auth.repository.UserRepository;
import com.culti.booking.entity.Booking;
import com.culti.booking.entity.BookingSeat;
import com.culti.booking.entity.Seat;
import com.culti.booking.repository.BookingRepository;
import com.culti.booking.repository.ScheduleRepository;
import com.culti.booking.repository.SeatRepository; // Seat 조회를 위해 필요
import com.culti.content.entity.Schedule;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository; // 추가

    @Transactional
    public void processPayment(Map<String, Object> paymentData, String loginEmail) {
        // 1. 유저 정보 조회 (auth.User 타입)
        User user = userRepository.findByEmail(loginEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 2. 스케줄 정보 조회
        Long scheduleId = Long.parseLong(paymentData.get("content_id").toString());
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("상영 스케줄을 찾을 수 없습니다."));

        // 3. 결제 데이터 추출
        Integer amount = Integer.parseInt(paymentData.get("total_price").toString());
        String merchantUid = (String) paymentData.get("merchant_uid");
        String impUid = (String) paymentData.get("imp_uid");
        String category = (String) paymentData.get("category");
        
        // 좌석 ID 리스트 (예: "101,102")
        String seatIdsStr = (String) paymentData.get("seat_info");
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
                .build();

        // 5. 개별 좌석(BookingSeat) 저장 로직
        for (String seatId : seatIdArray) {
            // 전달받은 좌석 ID로 실제 Seat 엔티티를 조회합니다.
            Seat seat = seatRepository.findById(Long.parseLong(seatId))
                    .orElseThrow(() -> new RuntimeException("좌석 정보를 찾을 수 없습니다."));

            BookingSeat bookingSeat = BookingSeat.builder()
                    .seat(seat)
                    .booking(booking)
                    .build();
            
            // Booking 엔티티 내 List에 추가 (Cascade 설정으로 같이 저장됨)
            booking.getBookingSeats().add(bookingSeat);
        }

        bookingRepository.save(booking);
    }
}