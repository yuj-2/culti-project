package com.culti.booking.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.culti.auth.entity.User; // [중요] SiteUser 대신 실제 회원 엔티티인 User를 임포트
import com.culti.content.entity.Schedule;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

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

    @Column(unique = true)
    private String merchantUid; // 포트원 주문번호 (CULTI_시간값)

    private String impUid;      // 포트원 결제 고유번호

    private String category;    // MOVIE, PERF, EXHIBIT 구분

    private Integer totalPrice;
    
    private String status;      // PAID, CANCEL 등

    private LocalDateTime createdAt;
    
    private Integer ticketCount;

    @Builder.Default
    @Column(nullable = false)
    private Integer discountAmount = 0;

    @Builder.Default
    @Column(nullable = false)
    private String paymentMethod = "CARD";

    @Builder.Default
    @Column(nullable = false)
    private String paymentStatus = "COMPLETED";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // [수정] SiteUser 타입을 User 타입으로 변경하여 타입 불일치 해결

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    // 생성 시 날짜 자동 입력
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}