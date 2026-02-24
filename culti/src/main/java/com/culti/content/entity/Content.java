package com.culti.content.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Content {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "content_id")
	private Long id;
	
	@Column(nullable = false, length = 20)
	private String category;
	
	@Column(nullable = false, length = 200)
	private String title;
	
	@Column(length = 500)
	private String posterUrl;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Column(length = 20)
	private String ageLimit;
	private Integer runningTime;
	
	@Column(nullable = false)
	private LocalDateTime createdAt;
	
	@Column(nullable = false)
	private LocalDate startDate;
	
	@Column(nullable = false)
	private LocalDate endDate;
	
	// 장소
	@OneToMany(mappedBy = "content")
    private List<Schedule> schedules;
	
}
