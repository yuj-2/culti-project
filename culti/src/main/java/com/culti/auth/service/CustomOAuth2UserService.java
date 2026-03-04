package com.culti.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.entity.SocialAuth;
import com.culti.auth.entity.User;
import com.culti.auth.repository.SocialAuthRepository;
import com.culti.auth.repository.UserRepository;
import com.culti.auth.security.PrincipalDetails;

import jakarta.servlet.http.HttpSession;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SocialAuthRepository socialAuthRepository;
    private final UserRepository userRepository;
    private final HttpSession session;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId;
        
        Long linkUserId = (Long) session.getAttribute("LINK_USER_ID");
        String mode = (String) session.getAttribute("OAUTH2_MODE");
        if ("kakao".equals(provider)) {
            providerId = attributes.get("id").toString();

        } else if ("google".equals(provider)) {
            providerId = attributes.get("sub").toString();

        } else if ("naver".equals(provider)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            providerId = response.get("id").toString();
            attributes = response;

        } else {
            throw new OAuth2AuthenticationException("Unsupported provider");
        }

        Optional<SocialAuth> socialAuthOpt =
            socialAuthRepository.findWithUserByProviderAndProviderId(provider, providerId);
        User user=null;
        if (socialAuthOpt.isEmpty()) {
           
        	if("link".equals(mode)) {
        		Optional<User> result=this.userRepository.findByUserId(linkUserId);
            	
            	if (result.isPresent()) {
    				user=result.get();
    			}
        	}else {
        		throw new UsernameNotFoundException("마이페이지에 연동이 안된 계정입니다.");
        	}
        	
        	
        	
        }else {
        	 user = socialAuthOpt.get().getUser(); // 이미 fetch join → 안전

             
             
        }
        
     // ✅ Entity → DTO 변환
        // 이미 있는코드지만... 임시방편으로
        UserDTO dto = UserDTO.builder()
 				.userId(user.getUserId())
 				.password(user.getPassword())
 				.phone(user.getPhone())
 				.name(user.getName())
 				.status(user.getStatus())
 				.role(user.getRole())
 				.birthDate(user.getBirthDate())
 				.gender(user.getGender())
 				.createdAt(user.getCreatedAt())
 				.nickname(user.getNickname())
 				.email(user.getEmail())
 				.build();

         return new PrincipalDetails(dto, attributes);
       
    }
}