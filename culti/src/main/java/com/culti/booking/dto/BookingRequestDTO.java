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
    private List<String> seatIds;
    private Integer totalPrice;

    // 🔥 추가
    private String impUid;
    private String merchantUid;
}