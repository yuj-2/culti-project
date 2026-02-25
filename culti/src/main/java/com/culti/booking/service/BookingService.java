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
import com.culti.booking.entity.Schedule;
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

     // BookingService.java 내 루프 부분 수정
        for (String seatIdStr : requestDTO.getSeatIds()) {
            BookingSeat bookingSeat = new BookingSeat();
            bookingSeat.setBooking(booking);
            
            try {
                Long seatId = Long.parseLong(seatIdStr);
                // findById로 못 찾을 경우 DB의 첫 번째 좌석을 대신 가져오도록 설정
                Seat seat = seatRepository.findById(seatId).orElseGet(() -> {
                    System.out.println("주의: ID " + seatId + "가 DB에 없어 임시 좌석으로 대체됨");
                    return seatRepository.findAll().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("DB에 좌석 데이터가 없습니다."));
                });
                bookingSeat.setSeat(seat);
            } catch (Exception e) {
                // 예외 발생 시 방어적으로 첫 번째 좌석 할당
                Seat seat = seatRepository.findAll().get(0);
                bookingSeat.setSeat(seat);
            }
            booking.getBookingSeats().add(bookingSeat);
        }
            


        // [중요] DB 저장 후 생성된 PK(Long)를 반드시 리턴해야 함
        Booking savedBooking = bookingRepository.save(booking);
        return savedBooking.getBookingId();
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
                // 담당자 엔티티 필드 반영 (Title)
                .movieTitle(booking.getSchedule().getContent().getTitle()) 
                .showTime(booking.getSchedule().getShowTime().toString())
                // 타임리프 th:each 반복문을 위해 리스트 전달
                .bookingSeats(booking.getBookingSeats()) 
                .build();
    }
}