package com.culti.booking.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookingRequestDTO {
    private Long scheduleId;
    private List<String> seatIds; // ★ Long에서 String으로 변경!
    private Integer totalPrice;
    private Long userId;
}