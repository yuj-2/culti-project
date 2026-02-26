package com.culti.booking.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.dto.BookingResponseDTO;
import com.culti.booking.entity.Seat;
import com.culti.booking.repository.SeatRepository;
import com.culti.booking.service.BookingService;
import com.culti.content.entity.Schedule;
import com.culti.content.service.ScheduleService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor	 
public class BookingController {

    private final BookingService bookingService;
    private final ScheduleService scheduleService; // [추가] 필드 선언
    private final SeatRepository seatRepository;   // [추가] 필드 선언

 
 // 1. 예매 메인 페이지 (두 주소 모두 이 메서드로 들어오게 설정)
    @GetMapping({"/reservation/booking", "/reservation/booking/performance"})
    public String bookingPage(Model model) {
        // templates/reservation/booking.html 파일을 보여줌
        return "reservation/booking"; 
    }

    // 2. 좌석 선택 페이지 (중복 통합 및 로직 완성)
    @GetMapping("/reservation/booking/seat")
    public String bookingSeatPage (
            @RequestParam(value = "scheduleId", required = false, defaultValue = "1") Long scheduleId, 
            Model model) {
        
        // [확인] 데이터가 없을 경우를 대비해 예외 처리를 하거나 더미 객체를 넘겨야 합니다.
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        
        if (schedule == null) {
            // 데이터가 없으면 리스트 페이지로 튕기거나 에러 메시지를 보여줘야 함
            return "redirect:/reservation/booking?error=notfound";
        }
        
        List<Seat> seats = seatRepository.findAll();
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("seats", seats);
        model.addAttribute("scheduleId", scheduleId); 
        
        return "reservation/booking_seat"; 
    }

    // 3. 예매 실행
    @PostMapping("/reservation/booking/create")
    public String createBooking(@ModelAttribute BookingRequestDTO requestDTO) {
        Long bookingId = bookingService.createBooking(requestDTO);
        return "redirect:/reservation/booking/result/" + bookingId;
    }

 // 4. 예매 완료 결과 페이지 수정
    @GetMapping("/reservation/booking/result/{id}")
    public String showBookingResult(@PathVariable("id") Long id, Model model) {
        BookingResponseDTO response = bookingService.getBookingResult(id);
        model.addAttribute("booking", response);
        
        // 예매 완료 페이지 하단에 "매점 상품 추천" 섹션을 넣기 위해 플래그 추가
        model.addAttribute("suggestStore", true); 
        
        return "reservation/booking_result"; 
    }
}