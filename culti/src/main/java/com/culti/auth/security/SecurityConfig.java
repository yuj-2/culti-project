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

import com.culti.auth.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@Configuration	   // 스프링 환경 설정 파일  == security-context.xml 동일
@EnableWebSecurity 	// 모든 요청 URL이 스프링 시큐리티 제어
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 애너테이션이 동작할 수 있도록 스프링 시큐리티의 설정
@RequiredArgsConstructor
public class SecurityConfig {
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(new AntPathRequestMatcher("/reservation/booking/**")).authenticated()
                
                // [수정] 결제 성공(/success)과 실패(/fail) 경로에 대한 권한을 명시적으로 추가했습니다.
                .requestMatchers(new AntPathRequestMatcher("/payment/**")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/success/**")).authenticated() 
                .requestMatchers(new AntPathRequestMatcher("/fail/**")).permitAll()
                
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
        
        // --- CSRF 설정 ---
        .csrf((csrf) -> csrf
            // [수정] 외부(토스 서버)에서 우리 서버로 결제 승인 요청(POST)을 보낼 때 
            // CSRF 토큰이 없어도 거부되지 않도록 '/payment/confirm/**' 경로를 예외 처리했습니다.
            .ignoringRequestMatchers(new AntPathRequestMatcher("/payment/confirm/**"))
            .csrfTokenRepository(new HttpSessionCsrfTokenRepository()))
        .oauth2Login(oauth2 -> oauth2
        	    .loginPage("/auth/login")
        	    .successHandler(oAuth2LoginSuccessHandler)
        	    .userInfoEndpoint(userInfo -> userInfo
        	        .userService(customOAuth2UserService)
        	    )
        	)
        
        // --- 세션 정책 추가 (응답 커밋 전 세션 생성 보장) ---
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
                    .invalidateHttpSession(true)) //로그아웃 시 현재 세션을 무효화합니다.
        ;
        return http.build();
    }

	// @Bean  등록
	// BCryptPasswordEncoder cryptPasswordEncoder = new BCryptPasswordEncoder();
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// <authentication-manager>
	@Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}