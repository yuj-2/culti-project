package com.culti.content.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Getter @Setter
@Table(name = "content_price")
public class ContentPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long id;

    // 어떤 콘텐츠(공연/전시/팝업)의 가격인지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id")
    @JsonIgnore
    private Content content;

    // 가격 등급
    @Column(nullable = false, length = 20)
    private String grade;

    // 해당 등급의 티켓 가격
    @Column(nullable = false)
    private Integer price;
}