package com.culti.booking.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.culti.booking.dto.PerformanceSeatResponseDTO;
import com.culti.booking.service.PerformanceSeatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PerformanceSeatApiController {

    private final PerformanceSeatService performanceSeatService;

    @GetMapping("/api/performance/seats/{scheduleId}")
    public List<PerformanceSeatResponseDTO> getSeats(
            @PathVariable("scheduleId") Long scheduleId){

        return performanceSeatService.getSeats(scheduleId);
    }
}