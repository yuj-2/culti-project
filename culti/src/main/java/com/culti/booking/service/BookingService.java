package com.culti.booking.service;

import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.auth.entity.User; // auth.User 엔티티 사용 확인
import com.culti.auth.repository.UserRepository;
import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.dto.BookingResponseDTO;
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
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;

    @Transactional
    public Long createBooking(BookingRequestDTO requestDTO, String email) {
        
        // [방어 코드 1] 필수 파라미터 null 체크 (500 에러 방지)
        if (requestDTO.getScheduleId() == null) {
            throw new IllegalArgumentException("상영 회차 정보(scheduleId)가 누락되었습니다.");
        }
        if (requestDTO.getSeatIds() == null || requestDTO.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("선택된 좌석 정보(seatIds)가 없습니다.");
        }

        // 1. 유저 및 스케줄 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음: " + email));

        Schedule schedule = scheduleRepository.findById(requestDTO.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("회차 정보 없음: " + requestDTO.getScheduleId()));

        // 2. Booking 객체 생성 (Builder 사용)
        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                // 기존 bookingNumber 필드 대신 merchantUid 사용
                .merchantUid("CULTI_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .totalPrice(requestDTO.getTotalPrice())
                .status("PAID")
                .paymentStatus("COMPLETED")
                .paymentMethod("CARD")
                .ticketCount(requestDTO.getSeatIds().size())
                .discountAmount(0)
                .build();

        // 3. 좌석 연결 (BookingSeat 저장)
        for (String seatIdStr : requestDTO.getSeatIds()) {
            if (seatIdStr == null || seatIdStr.trim().isEmpty()) continue;

            Long seatId = Long.parseLong(seatIdStr);
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("ID " + seatId + "번에 해당하는 좌석 정보가 DB에 없습니다."));

            BookingSeat bookingSeat = BookingSeat.builder()
                    .seat(seat)
                    .booking(booking)
                    .build();
            
            // Booking 엔티티의 리스트에 추가 (CascadeType.ALL 설정 확인 필요)
            booking.getBookingSeats().add(bookingSeat);
        }

        // 4. 저장 및 ID 반환
        return bookingRepository.save(booking).getBookingId();
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingResult(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예매 내역 없음: " + id));

        return BookingResponseDTO.builder()
                .bookingNumber(booking.getMerchantUid()) // merchantUid 필드 매핑
                .totalPrice(booking.getTotalPrice())
                .movieTitle(booking.getSchedule().getContent().getTitle()) 
                .showTime(booking.getSchedule().getShowTime().toString())
                .bookingSeats(booking.getBookingSeats()) 
                .build();
    }
}