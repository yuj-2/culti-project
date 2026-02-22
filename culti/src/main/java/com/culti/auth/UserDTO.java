package com.culti.auth;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class UserDTO {
	private Long userId;
	private String email;
	private String password;
	private String phone;
	private String name;
	private String status;
    private LocalDateTime createdAt;
	private String role;
	
	@DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss")
	private LocalDate birthDate; 
	
	private Character gender;
	private String nickname;
}
