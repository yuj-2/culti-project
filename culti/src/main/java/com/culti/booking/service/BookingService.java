package com.culti.booking.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.dto.BookingResponseDTO;
import com.culti.booking.entity.Booking;
import com.culti.booking.entity.BookingSeat;
import com.culti.content.entity.Schedule;
import com.culti.booking.entity.SiteUser;
import com.culti.booking.entity.Seat;
import com.culti.booking.repository.BookingRepository;
import com.culti.booking.repository.ScheduleRepository;
import com.culti.booking.repository.SiteUserRepository;
import com.culti.booking.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ScheduleRepository scheduleRepository;
    private final SiteUserRepository siteUserRepository;
    private final SeatRepository seatRepository;

    /**
     * 1. 예매 생성 로직
     */
    @Transactional
    public Long createBooking(BookingRequestDTO requestDTO) {
        // [DB 조회] 회차와 유저 정보 가져오기
        Schedule schedule = scheduleRepository.findById(requestDTO.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("회차 정보 없음: " + requestDTO.getScheduleId()));

        SiteUser user = siteUserRepository.findById(requestDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음: " + requestDTO.getUserId()));

        // [엔티티 생성] Booking 객체 세팅
        Booking booking = new Booking();
        booking.setBookingNumber("B" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setTotalPrice(requestDTO.getTotalPrice());
        booking.setTicketCount(requestDTO.getSeatIds().size());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setSchedule(schedule);
        booking.setUser(user);
        
        // DB 필수값(NOT NULL) 기본값 세팅
        booking.setStatus("PAID");
        booking.setPaymentMethod("CARD");
        booking.setPaymentStatus("COMPLETED");
        booking.setDiscountAmount(0);

        // [좌석 매핑] 전달받은 seatId 문자열을 실제 Seat 엔티티와 연결
        for (String seatIdStr : requestDTO.getSeatIds()) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            
            try {
                // 전달받은 값이 숫자 ID라면 해당 좌석 조회
                Long seatId = Long.parseLong(seatIdStr);
                Seat seat = seatRepository.findById(seatId)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석 ID: " + seatId));
                bookingSeat.setSeat(seat); // BookingSeat 엔티티의 @ManyToOne Seat seat 필드에 세팅
            } catch (NumberFormatException e) {
                // 숫자가 아닐 경우(예: "A1") 테스트를 위해 DB의 첫 번째 좌석 강제 할당
                Seat firstSeat = seatRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("DB에 Seat 데이터가 하나도 없습니다."));
                bookingSeat.setSeat(firstSeat);
            }
            
            booking.getBookingSeats().add(bookingSeat);
        }

        // DB 저장
        return bookingRepository.save(booking).getBookingId();
    }

    /**
     * 2. 예매 결과 조회 로직
     */
    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingResult(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예매 내역을 찾을 수 없습니다."));

        return BookingResponseDTO.builder()
                .bookingNumber(booking.getBookingNumber())
                .totalPrice(booking.getTotalPrice())
                // [수정] 이제 Schedule -> Content -> Title로 진짜 제목을 가져옵니다.
                .movieTitle(booking.getSchedule().getContent().getTitle()) 
                .showTime(booking.getSchedule().getShowTime().toString())
                // [수정] 실제 좌석의 행(Row)과 열(Col) 정보를 조합해서 출력
                .seatNames(booking.getBookingSeats().stream()
                        .map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatCol()) 
                        .collect(Collectors.toList()))
                .build();
    }
}