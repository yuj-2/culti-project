package com.culti.auth.service;

import lombok.extern.slf4j.Slf4j; // 로그 사용을 위한 어노테이션 (Lombok 사용 시)
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j // 로그 기능을 활성화합니다.
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스의 loadUser를 호출하여 사용자 정보를 가져옵니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 1. 서비스 구분 (예: kakao)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 2. 전체 데이터 가져오기 (Map 형태)
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 3. 카카오 데이터 파싱
        // 카카오는 id, kakao_account(email 등), profile(nickname 등) 구조로 데이터를 줍니다.
        String providerId = attributes.get("id").toString();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");

        // --- 콘솔 출력 구간 ---
        System.out.println("========================================");
        System.out.println("[OAuth2 로그인 발생]");
        System.out.println("서비스 이름 (Registration ID): " + registrationId);
        System.out.println("카카오 고유 ID (Provider ID): " + providerId);
        System.out.println("사용자 닉네임: " + nickname);
        
        // 이메일은 카카오 설정에서 '필수'로 되어 있어야 가져올 수 있습니다.
        if (kakaoAccount.get("email") != null) {
            System.out.println("사용자 이메일: " + kakaoAccount.get("email"));
        }

        // 전체 데이터를 보고 싶을 때 (디버깅용)
        System.out.println("전체 Attributes: " + attributes);
        System.out.println("========================================");

        return oAuth2User;
    }
}