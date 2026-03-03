package com.culti.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {

    // 예매번호 (merchantUid 또는 생성한 bookingNumber)
    private String bookingNumber;

    // 영화 제목
    private String movieTitle;

    // 상영 시간
    private String showTime;

    // 총 결제 금액
    private Integer totalPrice;

    // 좌석 이름 리스트 (예: A1, A2, B3)
    private List<String> seatNames;
}