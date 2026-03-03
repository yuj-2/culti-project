package com.culti.auth.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TermsDTO {
	private Long id;
    private String title;
    private String content;
    private String isRequired;
    private String version;
    private LocalDateTime createdAt;
}
