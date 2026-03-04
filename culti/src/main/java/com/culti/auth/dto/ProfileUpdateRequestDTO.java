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
public class ProfileUpdateRequestDTO {
	private String name;
	private String email;
    private String nickname;
    private String phone;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    private Character gender;
    
}
