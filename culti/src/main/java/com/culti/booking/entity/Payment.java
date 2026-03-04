package com.culti.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "PAYMENT_HISTORY") // Oracle 테이블명
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String paymentKey;
    private Long amount;
    private String status; // DONE, CANCEL 등
}