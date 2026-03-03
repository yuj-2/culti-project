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
        .authorizeHttpRequests((auth) -> auth
                // /api를 뺀 주소로 정확히 매칭하세요.
                .requestMatchers(new AntPathRequestMatcher("/payment/verify/**")).permitAll() 
                .requestMatchers(new AntPathRequestMatcher("/reservation/booking/**")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
        
        .csrf((csrf) -> csrf
                // POST 요청 허용을 위해 CSRF 예외 대상에도 추가합니다.
                .ignoringRequestMatchers(new AntPathRequestMatcher("/payment/verify/**"))
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