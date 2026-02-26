package com.culti.support.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import lombok.*;

enum InquiryStatus {
    PENDING, ANSWERED
}

@Entity
@Table(name = "inquiry")
@Getter 
@Setter
@NoArgsConstructor
public class Inquiry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long inquiryId; 

    @Column(name = "user_id")
    private Long userId; 
    
    @Column(name = "inquiry_title")
    private String inquiryTitle;
    
    @Column(name = "inquiry_content", columnDefinition = "TEXT")
    private String inquiryContent;
    
    @Column(name = "inquiry_answer")
    private String inquiryAnswer;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_status")
    private InquiryStatus inquiryStatus = InquiryStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Inquiry(Long userId, String inquiryTitle, String inquiryContent) {
        this.userId = userId;
        this.inquiryTitle = inquiryTitle;
        this.inquiryContent = inquiryContent;
        this.inquiryStatus = InquiryStatus.PENDING;
    }
}