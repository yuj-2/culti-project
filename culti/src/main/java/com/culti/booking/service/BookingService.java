package com.culti.booking.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.auth.entity.User;
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
        if (requestDTO.getScheduleId() == null) {
            throw new IllegalArgumentException("상영 회차 정보(scheduleId)가 누락되었습니다.");
        }
        if (requestDTO.getSeatIds() == null || requestDTO.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("선택된 좌석 정보(seatIds)가 없습니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음: " + email));

        Schedule schedule = scheduleRepository.findById(requestDTO.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("회차 정보 없음: " + requestDTO.getScheduleId()));

        Booking booking = Booking.builder()
                .user(user)
                .schedule(schedule)
                .merchantUid("CULTI_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .totalPrice(requestDTO.getTotalPrice())
                .status("PAID")
                .paymentStatus("COMPLETED")
                .paymentMethod("CARD")
                .ticketCount(requestDTO.getSeatIds().size())
                .discountAmount(0)
                .build();

        for (String seatIdStr : requestDTO.getSeatIds()) {
            if (seatIdStr == null || seatIdStr.trim().isEmpty()) continue;

            Long seatId = Long.parseLong(seatIdStr);
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("ID " + seatId + "번에 해당하는 좌석 정보가 DB에 없습니다."));

            BookingSeat bookingSeat = BookingSeat.builder()
                    .seat(seat)
                    .booking(booking)
                    .build();
            
            booking.getBookingSeats().add(bookingSeat);
        }

        return bookingRepository.save(booking).getBookingId();
    }

    /**
     * [수정 완료] 에러 해결 버전
     */
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingResult(Long id) {
        // 1. 예약 정보 조회
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예매 내역 없음: " + id));

        // 2. 좌석 이름 리스트 추출 (seatRow와 seatCol을 합침)
        // seatRow(String) + seatCol(Integer) 조합 예: "A" + 1 = "A1"
        List<String> seatNames = booking.getBookingSeats().stream()
                .map(bs -> {
                    Seat seat = bs.getSeat();
                    return seat.getSeatRow() + seat.getSeatCol(); 
                })
                .collect(java.util.stream.Collectors.toList());

        // 3. DTO 빌더 구성
        return BookingResponseDTO.builder()
                .bookingNumber(booking.getMerchantUid())
                // Schedule 엔티티 구조에 따라 getContent() 확인
                .movieTitle(booking.getSchedule().getContent().getTitle()) 
                .showTime(booking.getSchedule().getStartTime().toString())
                .totalPrice(booking.getTotalPrice())
                .seatNames(seatNames) 
                .build();
    }
}