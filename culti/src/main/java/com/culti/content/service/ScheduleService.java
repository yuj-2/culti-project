package com.culti.content.service;

import com.culti.content.entity.Schedule;
import com.culti.booking.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service // 인터페이스 없이 바로 서비스로 등록
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    // 상세페이지나 좌석 선택 페이지에서 영화/공연 정보를 가져오기 위한 메서드
    public Schedule getScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상영 일정을 찾을 수 없습니다. ID: " + scheduleId));
    }
}