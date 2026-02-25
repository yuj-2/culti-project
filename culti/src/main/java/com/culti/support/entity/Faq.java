package com.culti.support.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faq")
@Getter 
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 기본 생성자 호출 제한
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faq_id")
    private Long faqId; //

    @Column(name = "faq_category", nullable = false)
    private String faqCategory; //

    @Column(name = "faq_content", columnDefinition = "TEXT", nullable = false)
    private String faqContent; //

    @Column(name = "faq_answer", columnDefinition = "TEXT", nullable = false)
    private String faqAnswer; //

    @Builder
    public Faq(String faqCategory, String faqContent, String faqAnswer) {
        this.faqCategory = faqCategory;
        this.faqContent = faqContent;
        this.faqAnswer = faqAnswer;
    }
}