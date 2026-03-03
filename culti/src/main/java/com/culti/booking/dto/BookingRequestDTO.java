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

    // 🔥 4종 인원
    private Integer adultCount;
    private Integer youthCount;
    private Integer seniorCount;
    private Integer specialCount;

    private Integer totalPrice;

    private String impUid;
    private String merchantUid;
}