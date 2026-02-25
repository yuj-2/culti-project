package com.culti.booking.dto;

import lombok.*;
import java.util.List;
import com.culti.booking.entity.BookingSeat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {
    private String bookingNumber;
    private String movieTitle;
    private String showTime;
    private Integer totalPrice;
    private List<String> seatNames; // 가공된 이름 리스트
    private List<BookingSeat> bookingSeats; // 타임리프 반복문용 리스트
}