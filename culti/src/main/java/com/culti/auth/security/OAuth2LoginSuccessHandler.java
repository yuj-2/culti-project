package com.culti.auth.security;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.culti.auth.entity.SocialAuth;
import com.culti.auth.entity.User;
import com.culti.auth.repository.SocialAuthRepository;
import com.culti.auth.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final SocialAuthRepository socialAuthRepository;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
    	
    	System.out.println("소셜로그인성공메서드진입");
    	
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = authToken.getPrincipal();

        String provider = authToken.getAuthorizedClientRegistrationId(); // kakao/google/naver
        String providerId = extractProviderId(provider, oAuth2User);

        HttpSession session = request.getSession();

        // ✅ 세션에서 mode와 link 대상 userId 가져오기
        String mode = (String) session.getAttribute("OAUTH2_MODE");
        Long linkUserId = (Long) session.getAttribute("LINK_USER_ID");

        // ✅ 사용 후 반드시 제거
        session.removeAttribute("OAUTH2_MODE");
        session.removeAttribute("LINK_USER_ID");

        Optional<SocialAuth> socialAuth =
                socialAuthRepository.findByProviderAndProviderId(provider, providerId);

        // ✅ 이미 연동된 소셜 계정이면 그냥 로그인
        if (socialAuth.isPresent()) {
            setDefaultTargetUrl("/home");
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        // ✅ 연동 모드인 경우
        if ("link".equals(mode)) {
        	System.out.println("연동모드");
            if (linkUserId == null) {
                throw new RuntimeException("연동 대상 사용자 정보가 세션에 없습니다.");
            }
            
            User user = userRepository.findById(linkUserId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            socialAuthRepository.save(
                    SocialAuth.builder()
                            .user(user)
                            .provider(provider)
                            .providerId(providerId)
                            .build()
                            
            );

            setDefaultTargetUrl("/auth/myPage?linked=success");
        } 
        // ❌ 연동도 아니고 기존 연동도 없는 경우 → 차단
        else {
            setDefaultTargetUrl("/auth/login?error=not_linked");
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String extractProviderId(String provider, OAuth2User oAuth2User) {
        if ("kakao".equals(provider)) {
            return oAuth2User.getAttribute("id").toString();
        }
        if ("google".equals(provider)) {
            return oAuth2User.getAttribute("sub");
        }
        if ("naver".equals(provider)) {

            Map<String, Object> attributes = oAuth2User.getAttributes();
            Map<String, Object> response =
                    (Map<String, Object>) attributes.get("response");

            if (response != null) {
                return response.get("id").toString();
            }

            // response가 없는 경우 (평탄화된 경우)
            return attributes.get("id").toString();
        }
        throw new IllegalArgumentException("Unsupported provider: " + provider);
    }
}