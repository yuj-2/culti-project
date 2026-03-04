package com.culti.booking.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;
    
    private Long userId; 
    private Long scheduleId;
    
    @Column(unique = true, nullable = false)
    private String bookingNumber; 

    private Integer totalPrice;
    
    // 초기값 직접 할당으로 에러 방지
    private String status = "PENDING"; 

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private Integer ticketCount = 0;
    private Integer discountAmount = 0;
    private String paymentMethod;
    
    @Column(name = "payment_status")
    private String paymentStatus;
}