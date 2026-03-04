package com.culti.booking.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {
    private String paymentKey; // 토스 결제 고유 키
    private String orderId;    // 주문 번호
    private Long amount;       // 결제 금액
}