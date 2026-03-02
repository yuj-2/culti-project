package com.culti.auth.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.culti.auth.dto.UserDTO;

import lombok.Getter;

@Getter
public class PrincipalDetails implements UserDetails, OAuth2User {

    private final UserDTO userDto;
    private Map<String, Object> attributes;

    // ✅ 일반 로그인용 생성자
    public PrincipalDetails(UserDTO userDto) {
        this.userDto = userDto;
    }

    // ✅ 소셜 로그인용 생성자
    public PrincipalDetails(UserDTO userDto, Map<String, Object> attributes) {
        this.userDto = userDto;
        this.attributes = attributes;
    }

    /* ================= OAuth2User ================= */

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    // OAuth2User의 name (식별자)
    @Override
    public String getName() {
        return userDto.getEmail(); // 통일
    }

    /* ================= UserDetails ================= */

    @Override
    public String getUsername() {
        return userDto.getEmail();
    }

    @Override
    public String getPassword() {
        return userDto.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ✅ ROLE 반드시 있어야 403 안 남
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}