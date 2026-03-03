package com.culti.auth.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "terms")
@Getter
@NoArgsConstructor
public class Terms extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_id")
    private Long id;

    @Column(name = "terms_title", length = 100, nullable = false)
    private String title;

    @Lob // TEXT 타입 매핑
    @Column(name = "terms_content", columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(name = "is_required", length = 1, nullable = false)
    private String isRequired; // 'Y' or 'N' (CHAR(1))

    @Column(name = "version", length = 10)
    private String version;

    @Column(name = "is_active", length = 1, nullable = false)
    private String isActive; // 'Y' or 'N' (CHAR(1))

}