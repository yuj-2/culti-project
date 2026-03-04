package com.culti.booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.culti.booking.repository.ScheduleRepository;
import com.culti.content.entity.ContentPrice;
import com.culti.content.entity.Schedule;
import com.culti.content.repository.ContentPriceRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/performance")
public class PerformancePriceApiController {

    private final ScheduleRepository scheduleRepository;
    private final ContentPriceRepository contentPriceRepository;

    @GetMapping("/price/{scheduleId}")
    public List<ContentPrice> getPrices(@PathVariable("scheduleId") Long scheduleId) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("schedule 없음"));

        Long contentId = schedule.getContent().getId();

        return contentPriceRepository.findByContentId(contentId);
}
}