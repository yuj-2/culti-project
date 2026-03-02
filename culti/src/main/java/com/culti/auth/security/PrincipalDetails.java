package com.culti.auth.security;


import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.culti.auth.dto.UserDTO;

import lombok.Getter;

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final UserDTO userDto; // 기존 DTO를 여기에 담습니다.
    private Map<String, Object> attributes; // 소셜 로그인용 정보 저장

    // 일반 로그인용 생성자
    public PrincipalDetails(UserDTO userDto) {
        this.userDto = userDto;
    }

    // 소셜 로그인용 생성자
    public PrincipalDetails(UserDTO userDto, Map<String, Object> attributes) {
        this.userDto = userDto;
        this.attributes = attributes;
    }

    // --- OAuth2User 구현 (소셜 전용) ---
    @Override public Map<String, Object> getAttributes() { return attributes; }
    @Override public String getName() { return userDto.getEmail(); }

    // --- UserDetails 구현 (일반 전용) ---
    @Override public String getPassword() { return userDto.getPassword(); }
    @Override public String getUsername() { return userDto.getEmail(); }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}