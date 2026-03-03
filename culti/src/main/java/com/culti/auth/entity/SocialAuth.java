package com.culti.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_auth")
@Getter // 모든 필드의 Getter를 자동 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자
@Builder // 빌더 패턴으로 객체 생성을 편하게 함
public class SocialAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long socialAuthId;

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 권장!
    @JoinColumn(name = "user_id") // DB의 외래키 컬럼명
    private User user;

    private String provider;
    private String providerId;
}