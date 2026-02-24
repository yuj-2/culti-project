package com.culti.booking.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BookingResponseDTO {
    private String bookingNumber;    // 생성된 예매번호 (예: B20260224)
    private String movieTitle;       // 영화/공연 제목
    private String showTime;         // 상영 일시
    private List<String> seatNames;  // 선택한 좌석 이름들 ["A1", "A2"]
    private int totalPrice;          // 최종 결제 금액
}