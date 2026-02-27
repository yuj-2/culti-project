package com.culti.support.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@Setter // 데이터를 수정하거나 업데이트할 때 필요합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (안정성 확보)
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id") // DB 컬럼명과 매핑 명시
    private Long noticeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notice_title", nullable = false)
    private String noticeTitle;

    @Column(name = "notice_content", columnDefinition = "TEXT", nullable = false)
    private String noticeContent;

    @Column(name = "view_count")
    private int viewCount = 0; // 초기값 설정

    @CreationTimestamp // INSERT 시 현재 시간 자동 저장
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notice(Long userId, String noticeTitle, String noticeContent) {
        this.userId = userId;
        this.noticeTitle = noticeTitle;
        this.noticeContent = noticeContent;
        this.viewCount = 0; // 생성 시 조회수 초기화
    }
}