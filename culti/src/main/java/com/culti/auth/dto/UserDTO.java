package com.culti.auth.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.culti.auth.entity.User;

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
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthDate; 
	
	private Character gender;
	private String nickname;
	
	public static UserDTO fromEntity(User user) {
        if (user == null) return null;

        return UserDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .password(user.getPassword()) // 시큐리티 인증용
                .phone(user.getPhone())
                .name(user.getName())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .role(user.getRole())
                .birthDate(user.getBirthDate())
                .gender(user.getGender())
                .nickname(user.getNickname())
                .build();
    }
}
