package com.culti.calendar.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.culti.booking.repository.ScheduleRepository;
import com.culti.calendar.dto.CalendarEventDTO;
import com.culti.content.entity.Schedule;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final ScheduleRepository scheduleRepository;

    @GetMapping("")
    public String calendarPage() {
        return "calendar/calendar";
    }

    @GetMapping("/api/events")
    @ResponseBody
    public List<CalendarEventDTO> getEvents(
    		@RequestParam(name = "year") int year,
            @RequestParam(name = "month") int month,
            @RequestParam(name = "category", defaultValue = "전체") String category
    ) {

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        List<Schedule> schedules =
                scheduleRepository.findByShowTimeGreaterThanEqualAndShowTimeLessThan(start, end);

        return schedules.stream()
            .filter(s -> "전체".equals(category) || s.getContent().getCategory().equals(category))
            .map(s -> new CalendarEventDTO(
                    s.getScheduleId(),
                    s.getContent().getId(),         // Content PK
                    s.getContent().getTitle(),
                    s.getShowTime().toLocalDate(),  // 캘린더 날짜
                    s.getContent().getCategory(),
                    s.getContent().getAgeLimit(),
                    s.getContent().getPosterUrl()
            ))
            .toList();
    }
}