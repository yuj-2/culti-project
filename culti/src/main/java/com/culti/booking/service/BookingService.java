package com.culti.booking.service;

import java.time.LocalDateTime;
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
import com.culti.booking.repository.BookingSeatRepository;
import com.culti.booking.repository.ScheduleRepository;
import com.culti.booking.repository.ScheduleSeatRepository;
import com.culti.booking.repository.SeatRepository;
import com.culti.content.entity.Schedule;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final ScheduleRepository scheduleRepository;

    /**
     * 예매 생성 (결제 전)
     */
    @Transactional
    public Long createBooking(BookingRequestDTO requestDTO, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 없음"));

        List<Long> seatIds = requestDTO.getSeatIds()
                .stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // 이미 예약된 좌석 확인
        boolean exists = scheduleSeatRepository
                .existsBySchedule_ScheduleIdAndSeat_SeatIdInAndStatus(
                        requestDTO.getScheduleId(),
                        seatIds,
                        "OCCUPIED"
                );

        if (exists) {
            throw new IllegalStateException("이미 예매된 좌석입니다.");
        }

        Booking booking = new Booking();
        booking.setUserId(user.getUserId());
        booking.setScheduleId(requestDTO.getScheduleId());

        String bookingNumber = "CULTI_" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();

        booking.setBookingNumber(bookingNumber);
        booking.setTotalPrice(requestDTO.getTotalPrice());
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setTicketCount(requestDTO.getSeatIds().size());
        booking.setDiscountAmount(0);
        booking.setPaymentMethod("TOSS");
        booking.setPaymentStatus("READY");
        
        Booking savedBooking = bookingRepository.save(booking);

        // 좌석 저장
        List<Seat> seats = seatRepository.findAllById(seatIds);

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> {
                    BookingSeat bs = new BookingSeat();
                    bs.setBooking(savedBooking);
                    bs.setSeat(seat);
                    return bs;
                })
                .collect(Collectors.toList());

        bookingSeatRepository.saveAll(bookingSeats);

        return savedBooking.getBookingId();
    }

    /**
     * 결제 완료 후 상태 변경
     */
    @Transactional
    public void confirmBookingStatus(String bookingNumber) {

        Booking booking = bookingRepository
                .findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new IllegalArgumentException("예매 없음"));

        booking.setStatus("PAID");

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingBookingId(booking.getBookingId());

        List<Long> seatIds = bookingSeats.stream()
                .map(bs -> bs.getSeat().getSeatId())
                .collect(Collectors.toList());

        scheduleSeatRepository.updateStatusToOccupied(
                booking.getScheduleId(),
                seatIds
        );
    }

    /**
     * 예매 결과 조회
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getMyBookings(Long userId) {

        List<Booking> bookings =
                bookingRepository.findByUserIdAndStatus(userId, "PAID");

        return bookings.stream().map(booking -> {

            Schedule schedule = scheduleRepository
                    .findById(booking.getScheduleId())
                    .orElseThrow();

            List<BookingSeat> bookingSeats =
                    bookingSeatRepository.findByBookingBookingId(booking.getBookingId());

            List<String> seatNames = bookingSeats.stream()
                    .map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatCol())
                    .toList();

            return BookingResponseDTO.builder()
                    .bookingNumber(booking.getBookingNumber())
                    .movieTitle(schedule.getContent().getTitle())
                    .showTime(schedule.getShowTime().toString())
                    .seatNames(seatNames)
                    .totalPrice(booking.getTotalPrice())
                    .build();

        }).toList();
    }

    
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getCancelledBookings(Long userId) {

        List<Booking> bookings = bookingRepository.findByUserIdAndStatus(userId, "CANCELLED");

        return bookings.stream().map(booking -> {

            Schedule schedule = scheduleRepository
                    .findById(booking.getScheduleId())
                    .orElseThrow(() -> new IllegalArgumentException("상영 정보 없음"));

            List<BookingSeat> bookingSeats =
                    bookingSeatRepository.findByBookingBookingId(booking.getBookingId());

            List<String> seatNames = bookingSeats.stream()
                    .map(bs -> bs.getSeat().getSeatRow() + bs.getSeat().getSeatCol())
                    .collect(Collectors.toList());

            return BookingResponseDTO.builder()
                    .bookingNumber(booking.getBookingNumber())
                    .movieTitle(schedule.getContent().getTitle())
                    .showTime(schedule.getShowTime().toString())
                    .seatNames(seatNames)
                    .totalPrice(booking.getTotalPrice())
                    .build();

        }).collect(Collectors.toList());
    }
    @Transactional
    public void cancelBooking(String bookingNumber) {

        Booking booking = bookingRepository
                .findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new IllegalArgumentException("예매 없음"));

        booking.setStatus("CANCELLED");
        
        // 2. 예매 좌석 조회
        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBookingBookingId(booking.getBookingId());

        // 3. seatId 추출
        List<Long> seatIds = bookingSeats.stream()
                .map(bs -> bs.getSeat().getSeatId())
                .collect(Collectors.toList());

        // 4. 좌석 다시 열기
        scheduleSeatRepository.updateStatusToAvailable(
                booking.getScheduleId(),
                seatIds
        );   

    }

}