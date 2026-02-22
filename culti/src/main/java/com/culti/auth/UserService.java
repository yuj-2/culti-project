package com.culti.auth;

import java.time.LocalDate;


public interface UserService {
	// DTO -> Entity 변환 메서드
		default User dtoToEntity(UserDTO dto) {
			
			User entity = User.builder()
					.userId(dto.getUserId())
					.email(dto.getEmail())
					.password(dto.getPassword())
					.phone(dto.getPhone())
					.name(dto.getName())
					.status(dto.getStatus())
					.role(dto.getRole())
					.birthDate(dto.getBirthDate())
					.gender(dto.getGender())
					.nickname(dto.getNickname())
					.build();		
			return entity;
		}

		// Entity -> DTO 변환 메서드
		default UserDTO entityToDto(User user) {
			

			/*
			private Long userId;
			
			private String email;
			private String password;
			private String phone;
			private String name;
			private String status;
			private String role;
			private LocalDate birthDate; 
			private Character gender;
			*/
			UserDTO dto = UserDTO.builder()
					.userId(user.getUserId())
					.password(user.getPassword())
					.phone(user.getPhone())
					.name(user.getName())
					.status(user.getStatus())
					.role(user.getRole())
					.birthDate(user.getBirthDate())
					.gender(user.getGender())
					.createdAt(user.getCreatedAt())
					.nickname(user.getNickname())
					.build();		
			return dto;
		} 
		
		// [1] 회원가입 (회원 등록)
		Long register(UserDTO userDTO);
		
		//[2] 로그인 
		UserDTO login(String email,String password);
		
}
