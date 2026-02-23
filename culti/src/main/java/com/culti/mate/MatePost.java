package com.culti.mate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString(exclude = "writer")
@Table(name = "mate_post")
public class MatePost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long postId;
	
	@Column(nullable = false)
    private String title;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
    private MatePostCategory category;

	
	@Column(nullable = false)
    private LocalDateTime eventAt;
    
	@Column(nullable = false)
    private String location;

	@Column(nullable = false)
    private int maxPeople;


    @Column(columnDefinition = "TEXT")
    private String description;


    @Enumerated(EnumType.STRING)
    private MatePostStatus status;
    
    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "writer_id", nullable = false)
//    private Users writer;
}
