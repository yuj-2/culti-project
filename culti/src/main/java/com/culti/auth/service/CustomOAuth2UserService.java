package com.culti.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 사용을 위한 어노테이션 (Lombok 사용 시)
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.entity.SocialAuth;
import com.culti.auth.entity.User;
import com.culti.auth.repository.SocialAuthRepository;
import com.culti.auth.repository.UserRepository;
import com.culti.auth.security.PrincipalDetails;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository; 
    private final SocialAuthRepository socialAuthRepository; // 추가된 Repository

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 1. 서비스 구분 및 데이터 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // 카카오 데이터 파싱
        String providerId = attributes.get("id").toString();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");

        // 2. 이메일 누락 체크
        if (email == null) {
            throw new OAuth2AuthenticationException("이메일 제공에 동의해야 로그인이 가능합니다.");
        }

        // 3. [핵심 로직] 우리 DB(users 테이블)에 이메일이 있는지 확인
        // 만약 DTO 대신 Entity를 직접 써도 된다면 findByEmail이 User를 반환하게 하세요.
        Optional<User> result = this.userRepository.findByEmail(email); 
        User user=null;
        if (result.isPresent()) {
			user=result.get();
		}
        
        
        if (user == null) {
            log.info("미가입 사용자 로그인 시도: {}", email);
            throw new OAuth2AuthenticationException("기존 회원 정보가 없습니다. 먼저 홈페이지 가입을 완료해주세요.");
        }

        // 4. [연동 로직] social_auth 테이블에서 해당 유저의 카카오 연동 여부 확인
        // existsByUserAndProvider 메서드는 Repository에 선언해야 합니다.
        boolean isLinked = socialAuthRepository.existsByUserAndProvider(user, registrationId);

        if (!isLinked) {
            // 연동 데이터가 없으면 새로 생성해서 저장
            SocialAuth newAuth = SocialAuth.builder()
                    .user(user) // User 엔티티와 연관관계 매핑
                    .provider(registrationId)
                    .providerId(providerId)
                    .build();
            
            socialAuthRepository.save(newAuth);
            log.info("기존 회원(이메일: {})에 {} 계정 연동 완료", email, registrationId);
        }

        UserDTO userDto = UserDTO.fromEntity(user);
        
        // 5. 로그인 성공 처리
        return new PrincipalDetails(userDto, attributes);
    }
}