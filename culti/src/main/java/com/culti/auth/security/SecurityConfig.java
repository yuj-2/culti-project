package com.culti.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(new AntPathRequestMatcher("/reservation/booking/**")).authenticated()
                // 1. 결제 검증 API 경로는 로그인 여부와 상관없이 접근 가능해야 안전합니다 (포트원 서버 통신 대비)
                .requestMatchers(new AntPathRequestMatcher("/payment/verify/**")).permitAll() 
                .requestMatchers(new AntPathRequestMatcher("/payment/**")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
        
        .csrf((csrf) -> csrf
                // 2. 결제 검증 POST 요청이 CSRF 토큰 때문에 차단되지 않도록 예외 경로 설정
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/payment/verify/**"),
                    new AntPathRequestMatcher("/payment/verify")
                )
                .csrfTokenRepository(new HttpSessionCsrfTokenRepository()))
        
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        
        .headers((headers) -> headers
                .addHeaderWriter(new XFrameOptionsHeaderWriter(
                        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
        
        .formLogin((formLogin) -> formLogin
                .loginPage("/auth/login") 
                .usernameParameter("email")
                .defaultSuccessUrl("/home"))
        
        .logout((logout) -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout"))
                .logoutSuccessUrl("/home")
                .invalidateHttpSession(true))
        ;
        
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}