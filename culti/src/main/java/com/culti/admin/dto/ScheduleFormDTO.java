package com.culti.admin.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter 
@Setter 
@ToString
public class ScheduleFormDTO {
    
    private Integer sessionNum;
    private String roomName;
    
    // HTML의 <input type="datetime-local"> 은 자동으로 LocalDateTime으로 찰떡같이 변환됩니다.
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}