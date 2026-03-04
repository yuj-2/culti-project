package com.culti.booking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.dto.BookingResponseDTO;
import com.culti.booking.entity.ScheduleSeat;
import com.culti.booking.entity.Seat;
import com.culti.booking.repository.ScheduleSeatRepository;
import com.culti.booking.repository.SeatRepository;
import com.culti.booking.service.BookingService;
import com.culti.content.entity.Schedule;
import com.culti.content.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final ScheduleService scheduleService;
    private final SeatRepository seatRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;

    /**
     * 예매 페이지
     */
    @GetMapping({"/reservation/booking", "/reservation/booking/performance"})
    public String bookingPage(Model model) {
        return "reservation/booking";
    }

    /**
     * 좌석 선택 페이지
     */
    @GetMapping("/reservation/booking/seat")
    public String bookingSeatPage(
            @RequestParam(value = "scheduleId", required = false, defaultValue = "1") Long scheduleId,
            Model model) {

        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        if (schedule == null) {
            return "redirect:/reservation/booking?error=notfound";
        }

        // 공통 모델
        model.addAttribute("schedule", schedule);
        model.addAttribute("scheduleId", scheduleId);

        // ✅ 카테고리로 좌석 페이지 분기
        String category = schedule.getContent().getCategory(); // "영화" / "공연" / "전시"

        if ("공연".equals(category)) {
            // 공연은 JS가 /api/performance/seats/{scheduleId}로 가져가니까 서버에서 좌석리스트 굳이 안 넣어도 됨
            return "reservation/booking_performance"; // ← 공연 좌석 HTML(지호님이 올린 그 파일)
        }

        // 영화(기존 방식) - 서버에서 좌석/상태 내려주는 템플릿 유지
        List<Seat> seats = seatRepository.findAll();
        List<ScheduleSeat> scheduleSeats = scheduleSeatRepository.findBySchedule_ScheduleId(scheduleId);
        model.addAttribute("seatListFromDb", seats);
        model.addAttribute("scheduleSeats", scheduleSeats);

        return "reservation/booking_seat";
    }
    /**
     * 좌석 선택 후 결제 요청 (fetch JSON)
     */
    @PostMapping("/reservation/booking/create/json")
    @ResponseBody
    public ResponseEntity<BookingResponseDTO> createBookingJson(
            @ModelAttribute BookingRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long bookingId = bookingService.createBooking(requestDTO, userDetails.getUsername());
        BookingResponseDTO response = bookingService.getBookingResult(bookingId);

        return ResponseEntity.ok(response);
    }

    /**
     * 기존 폼 방식 결제
     */
    @PostMapping("/reservation/booking/create")
    public String createBooking(
            @ModelAttribute BookingRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Long bookingId = bookingService.createBooking(requestDTO, userDetails.getUsername());
        BookingResponseDTO response = bookingService.getBookingResult(bookingId);

        model.addAttribute("booking", response);
        model.addAttribute("bookingId", bookingId);

        return "reservation/payment";
    }

    /**
     * 예매 완료 페이지
     */
    @GetMapping("/reservation/booking/result/{id}")
    public String showBookingResult(@PathVariable("id") Long id, Model model) {

        BookingResponseDTO response = bookingService.getBookingResult(id);

        model.addAttribute("booking", response);
        model.addAttribute("suggestStore", true);

        return "reservation/booking_result";
    }

    /**
     * 예매 취소 (마이페이지 버튼)
     * - thymeleaf form: th:action="@{/booking/cancel}" 와 정확히 매칭
     */
    @PostMapping("/booking/cancel")
    public String cancelBooking(@RequestParam("bookingNumber") String bookingNumber){

        bookingService.cancelBooking(bookingNumber);

        return "redirect:/auth/myPage?reservationTab=cancel";
    }
  

   
}