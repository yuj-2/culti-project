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

    @GetMapping({"/reservation/booking", "/reservation/booking/performance"})
    public String bookingPage(Model model) {
        return "reservation/booking"; 
    }

    @GetMapping("/reservation/booking/seat")
    public String bookingSeatPage (
            @RequestParam(value = "scheduleId", required = false, defaultValue = "1") Long scheduleId, 
            Model model) {
        
        Schedule schedule = scheduleService.getScheduleById(scheduleId);
        if (schedule == null) {
            return "redirect:/reservation/booking?error=notfound";
        }
        
        List<Seat> seats = seatRepository.findAll();
        model.addAttribute("schedule", schedule);
        model.addAttribute("seats", seats);
        model.addAttribute("scheduleId", scheduleId); 
        
        return "reservation/booking_seat"; 
    }

    // [수정] 결제 완료 후 호출되는 지점. 세션 유저 정보를 함께 넘깁니다.
    @PostMapping("/reservation/booking/create")
    public String createBooking(@ModelAttribute BookingRequestDTO requestDTO, 
                                @AuthenticationPrincipal UserDetails userDetails) {
        // 로그인한 유저의 이메일을 서비스로 전달
        Long bookingId = bookingService.createBooking(requestDTO, userDetails.getUsername());
        return "redirect:/reservation/booking/result/" + bookingId;
    }

    @GetMapping("/reservation/booking/result/{id}")
    public String showBookingResult(@PathVariable("id") Long id, Model model) {
        BookingResponseDTO response = bookingService.getBookingResult(id);
        model.addAttribute("booking", response);
        model.addAttribute("suggestStore", true); 
        return "reservation/booking_result"; 
    }
}