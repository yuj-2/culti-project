package com.culti.admin.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter 
@Setter 
@ToString
public class ContentFormDTO {
    
    private String category;
    private String title;
    private String ageLimit;
    private Long placeId;          // 화면에서 선택한 장소의 ID
    private Integer runningTime;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private MultipartFile posterFile; // 🌟 핵심! 파일을 받아줄 객체
    private String description;
    
    // 여러 개의 회차 정보를 리스트로 한 방에 받습니다!
    private List<ScheduleFormDTO> schedules; 
}
