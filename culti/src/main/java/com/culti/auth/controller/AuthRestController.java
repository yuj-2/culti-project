package com.culti.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.culti.auth.dto.ChangePasswordRequestDTO;
import com.culti.auth.dto.ProfileUpdateRequestDTO;
import com.culti.auth.entity.User;
import com.culti.auth.security.PrincipalDetails;
import com.culti.auth.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthRestController {
	
	private final UserService userService;
	
	@PostMapping("/myPage/update")
	public ResponseEntity<?> updateProfile(
	        @RequestBody ProfileUpdateRequestDTO request) {
	    System.out.println(request.getName());
	    System.out.println(request.getNickname());
	    System.out.println(request.getPhone());
	    System.out.println(request.getBirthdate());
	    System.out.println(request.getEmail());
	    System.out.println(request.getGender());
	    
	    /*
	    private String name;
	    private String nickname;
	    private String phone;
	    private String birthdate;
	    private String gender;
	    */
	    Authentication authentication =
	            SecurityContextHolder.getContext().getAuthentication();

	    PrincipalDetails principal =
	            (PrincipalDetails) authentication.getPrincipal();
	    
	    User user=this.userService.findEntityByEmail(request.getEmail());
	    
	    user.setNickname(request.getNickname());
	    user.setPhone(request.getPhone());
	    user.setBirthDate(request.getBirthdate());
	    user.setGender(request.getGender());
	    
	    this.userService.saveUser(user);
	    
	    principal.setDto(this.userService.entityToDto(user));
	    
	    
	    return ResponseEntity.ok(Map.of("result", "success"));
	}
	
	@PostMapping("/myPage/changePassword")
	public ResponseEntity<?> changePassword(
	        @RequestBody ChangePasswordRequestDTO request) {
	    
		System.out.println("비밀번호 변경 실행");
		String email=request.getEmail();
		String currentPassword=request.getCurrentPassword();
		String newPassword=request.getNewPassword();
		
		
		System.out.println(email);
		System.out.println(currentPassword);
		System.out.println(newPassword);
		
		
	    Authentication authentication =
	            SecurityContextHolder.getContext().getAuthentication();

	    PrincipalDetails principal =
	            (PrincipalDetails) authentication.getPrincipal();
	    
	    User user=this.userService.changePassword(email, currentPassword, newPassword);
	    
	    
	    
	    principal.setDto(this.userService.entityToDto(user));
	    
	    
	    return ResponseEntity.ok(Map.of("result", "success"));
	}
}
