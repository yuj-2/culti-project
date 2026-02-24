package com.culti.mate.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.culti.mate.enums.MateApplyStatus;

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
@ToString(exclude =  {"applicant", "post"})
@Table(name = "mate_apply")
public class MateApply {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long applyId;
	
	@Column
	private String message;
	
	@Column
	@Enumerated(EnumType.STRING)
	private MateApplyStatus status;
	
	@Column
	private LocalDateTime decidedAt;
	
	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private MatePost post;
	
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "applicant_id", nullable = false)
//	private Users applicant;
	
}
