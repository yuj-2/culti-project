package com.culti.content.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentDTO {
    private Long id;
    private String category;
    private String title;
    private String ageLimit;
    private Long placeId; // 화면에서 선택한 장소 번호
    private Integer runningTime;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    
    // 여러 개의 회차 정보를 담을 리스트!
    private List<ScheduleDTO> schedules; 
}
