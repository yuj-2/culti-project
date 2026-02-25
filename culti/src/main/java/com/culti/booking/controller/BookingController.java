package com.culti.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.culti.booking.dto.BookingRequestDTO;
import com.culti.booking.dto.BookingResponseDTO;
import com.culti.booking.service.BookingService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor // BookingService를 자동으로 가져옵니다.
public class BookingController {

    private final BookingService bookingService;

    // 1. 기존 예매 메인 페이지 (영화/날짜 선택)
    @GetMapping("/reservation/booking")
    public String bookingPage(Model model) {
        // (기존 영화/날짜/지역 더미 데이터 로직은 유지하거나, 나중에 서비스 호출로 교체 가능)
        return "reservation/booking"; 
    }

 // 2. 좌석 선택 페이지
    @GetMapping("/reservation/booking/seat")
    public String bookingSeatPage(
            @RequestParam(value = "scheduleId", required = false) Long scheduleId, 
            HttpServletRequest request, // 세션 생성을 위해 추가
            Model model) {
        
        // 타임리프가 폼(Action)을 그리기 전에 세션을 미리 생성하여 CSRF 에러를 방지합니다.
        request.getSession(true); 

        if (scheduleId == null) {
            scheduleId = 1L; 
        }
        
        model.addAttribute("scheduleId", scheduleId); 
        return "reservation/booking_seat"; 
    }

    // 3. [핵심] 예매 실행 (결제하기 버튼 클릭 시 호출)
    @PostMapping("/reservation/booking/create")
    public String createBooking(@ModelAttribute BookingRequestDTO requestDTO) {
        // 서비스 호출하여 DB에 저장하고 생성된 ID를 받아옴
        Long bookingId = bookingService.createBooking(requestDTO);
        
        // 저장 성공 후 결과 페이지로 리다이렉트 (ID 전달)
        return "redirect:/reservation/booking/result/" + bookingId;
    }

    // 4. 예매 완료 결과 페이지
    @GetMapping("/reservation/booking/result/{id}")
    public String showBookingResult(@PathVariable("id") Long id, Model model) {
        // 서비스에서 ResponseDTO를 받아옴
        BookingResponseDTO response = bookingService.getBookingResult(id);
        
        // HTML(접시)에 데이터(음식)를 담아서 보냄
        model.addAttribute("booking", response);
        return "reservation/booking_result"; 
    }

    @GetMapping("/reservation/booking/performance")
    public String bookingPerformancePage(Model model) {
        return "reservation/booking_performance"; 
    }
}	