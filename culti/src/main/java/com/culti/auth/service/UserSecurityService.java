package com.culti.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.auth.security.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSecurityService implements UserDetailsService{

	
	private final UserRepository userRepository;


	@Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 
		System.out.println("로그인 시도 이메일: [" + email + "]");
		Optional<User> _User = this.userRepository.findByEmail(email);
		
        if (_User.isEmpty()) {
        	System.out.println("에러발생");
            throw new UsernameNotFoundException("사용자를 찾을수 없습니다.");
        }
        
        User user = _User.get();
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole().equals("ADMIN")) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }
        
        // 레거시 프로젝트 수업 때 CustomUser 클래스 + 추가 정보 저장..
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
    }

}
