package com.culti.content.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduleDTO {
    private Long scheduleId;
    private Integer sessionNum;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
