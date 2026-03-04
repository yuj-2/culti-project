package com.culti.calendar.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarEventDTO {

    private Long scheduleId;
    private Long contentId;

    private String title;
    private LocalDate date;

    private String category;
    private String rating; // ageLimit
    private String image;  // posterUrl
}