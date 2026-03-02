package com.culti.auth.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

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
public class RegisterRequestDTO {
	private Long userId;
	private String email;
	private String password;
	private String phone;
	private String name;
	private String status;
    private LocalDateTime createdAt;
	private String role;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate; 
	
	private Character gender;
	private String nickname;
	
	// 약관 동의 정보: 사용자가 체크한 약관의 ID(Long)들만 리스트로 받음
    private List<Long> agreedTermIds;
}
