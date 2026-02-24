package com.culti.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration	   // 스프링 환경 설정 파일  == security-context.xml 동일
@EnableWebSecurity 	// 모든 요청 URL이 스프링 시큐리티 제어
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize 애너테이션이 동작할 수 있도록 스프링 시큐리티의 설정
public class SecurityConfig {
	
	@Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
            /* /h2-console/시작하는 모든 요청 URL은 CSRF 검증을 하지 않는다. */
            
            /*
            .csrf((csrf) -> csrf
                    .ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))*/
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




