package com.culti.booking.controller;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.dto.BookingResponseDTO;
import com.culti.booking.entity.Seat;
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

    /**
     * [통합 게이트웨이] 상세페이지에서 넘어오는 단일 예매 접점
     * 카테고리(영화/공연)에 따라 적절한 좌석 선택 HTML을 리턴합니다.
     */
    @GetMapping({"/reservation/booking/seat", "/reservation/booking/performance"})
    public String bookingGate(@RequestParam(value = "scheduleId") Long scheduleId, Model model) {
        
        // 1. 전달받은 ID로 스케줄 정보를 조회합니다.
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        if (schedule == null) {
            return "redirect:/reservation/booking?error=notfound";
        }
        
        // 2. 해당 콘텐츠의 카테고리를 확인하여 분기합니다.
        String category = schedule.getContent().getCategory();
        
        // 3. 공통 데이터를 모델에 담습니다.
        List<Seat> seats = seatRepository.findAll();
        model.addAttribute("schedule", schedule);
        model.addAttribute("seats", seats);
        model.addAttribute("scheduleId", scheduleId); 
        
        // 4. [핵심] 카테고리에 맞춰 리턴할 뷰(HTML)를 결정합니다.
        if ("영화".equals(category)) {
            // 카테고리가 '영화'일 경우 영화 전용 좌석 페이지 리턴
            return "reservation/booking_seat"; 
        } else {
            // 그 외(공연/전시 등)의 경우 공연 전용 좌석 페이지 리턴
            return "reservation/booking_performance"; 
        }
    }

    @PostMapping("/reservation/booking/create")
    public String createBooking(@ModelAttribute BookingRequestDTO requestDTO, 
                                @AuthenticationPrincipal UserDetails userDetails) {
        // 1. DB에 예매 정보(좌석 점유 등) 저장
        // 2. 생성된 예매 ID(bookingId) 반환
        Long bookingId = bookingService.createBooking(requestDTO, userDetails.getUsername());
        
        // 3. 결제창이 뜨는 결과 페이지로 리다이렉트
        return "redirect:/reservation/booking/result/" + bookingId;
    }

    @GetMapping("/reservation/booking/result/{id}")
    public String showBookingResult(@PathVariable("id") Long id, Model model) {
        BookingResponseDTO response = bookingService.getBookingResult(id);
        model.addAttribute("booking", response);
        model.addAttribute("suggestStore", true); 
        
        // 결과 페이지는 아까 완성한 바코드 흰색 배경 티켓 디자인 파일입니다.
        return "reservation/booking_result"; 
    }
}