package com.culti.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatInfoDTO {
    private Long seatId;
    private String seatRow;
    private Integer seatCol;
    private String grade;
    private Integer floor;
}