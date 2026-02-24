package com.culti.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter
@Setter
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    // 1. 어떤 콘텐츠(공연/전시)의 스케줄인가? (content_id FK)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    // 2. 이 스케줄은 어떤 장소에서 열리는가? (place_id FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // 3. 공연/전시 날짜 및 시간
    @Column(name = "show_time", nullable = false)
    private LocalDateTime showTime;

    // 4. 시작 시간
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    // 5. 종료 시간
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // 6. 회차 (예: 1회차, 2회차)
    @Column(name = "session_num", nullable = false)
    private Integer sessionNum;
}