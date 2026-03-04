package com.culti.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PerformanceSeatResponseDTO {
    private SeatInfoDTO seat;
    private String status; // AVAILABLE / OCCUPIED
}