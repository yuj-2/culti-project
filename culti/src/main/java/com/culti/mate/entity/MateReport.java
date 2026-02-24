package com.culti.mate.entity;

import java.time.LocalDateTime;

import com.culti.mate.enums.MateApplyStatus;
import com.culti.mate.enums.MateReportType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@ToString(exclude =  "reporter")
@Table(name = "mate_report")
public class MateReport {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long reportId;
	
	@Column
	@Enumerated(EnumType.STRING)
	private MateReportType targetType;
	
	@Column
	private Long targetId;
	
	@Column
	private String reason;
	
	@Column
	private String detail;
	
	@Column
	@Enumerated(EnumType.STRING)
	private MateApplyStatus status;
	
	@Column
	private LocalDateTime createdAt;
	
	@Column
	private LocalDateTime resolvedAt;
	
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "reporter_id")
//	private Users reporter;
}
