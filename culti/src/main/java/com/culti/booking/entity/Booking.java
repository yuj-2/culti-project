package com.culti.booking.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.culti.auth.entity.User; // [중요] SiteUser 대신 실제 회원 엔티티인 User를 임포트
import com.culti.content.entity.Schedule;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "booking")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    // DB 컬럼명과 일치시킴
    @Column(name = "booking_number", nullable = false, length = 50)
    private String bookingNumber;

    @Column(name = "merchant_uid", unique = true, nullable = false)
    private String merchantUid;

    @Column(name = "imp_uid", nullable = false)
    private String impUid;

    private String category;

    // DB 컬럼명 total_price와 매핑
    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;
    
    @Column(nullable = false)
    private String status; 

    private LocalDateTime createdAt;
    
    @Column(name = "ticket_count", nullable = false)
    private Integer ticketCount;

    @Builder.Default
    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount = 0;

    @Builder.Default
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod = "CARD";

    @Builder.Default
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus = "COMPLETED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}