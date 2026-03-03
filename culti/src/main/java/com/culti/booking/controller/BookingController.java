package com.culti.booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.culti.booking.repository.SeatRepository;
import com.culti.booking.service.BookingService;
import com.culti.content.entity.Schedule;
import com.culti.content.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation/booking")
public class BookingController {

    private final ScheduleService scheduleService;
    private final SeatRepository seatRepository;
    private final BookingService bookingService;

    @GetMapping("/seat")
    public String bookingSeat(@RequestParam("scheduleId") Long scheduleId, Model model) {
        Schedule schedule = scheduleService.getScheduleById(scheduleId);

        model.addAttribute("schedule", schedule);
        model.addAttribute("seats", seatRepository.findAll());
        model.addAttribute("scheduleId", scheduleId);

        return "reservation/booking_seat";
    }

    @GetMapping("/result/{id}")
    public String result(@PathVariable("id") Long id, Model model) {
        model.addAttribute("booking", bookingService.getBookingResult(id));
        return "reservation/booking_result";
    }
}