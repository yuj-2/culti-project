package com.culti.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.culti.auth.entity.SocialAuth;
import com.culti.auth.entity.User;

public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long>{
	
	// 특정 유저가 특정 소셜 업체(provider)로 이미 연동되어 있는지 확인
	boolean existsByUserAndProvider(User user, String provider);
	
	Optional<SocialAuth> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByProviderAndProviderId(String provider, String providerId);
    
    @Query("select sa from SocialAuth sa join fetch sa.user where sa.provider = :provider and sa.providerId = :providerId")
    Optional<SocialAuth> findWithUserByProviderAndProviderId(
        @Param("provider") String provider,
        @Param("providerId") String providerId
    );
    
    //특정 유저가 현재 소셜 로그인 데이터가 저장이 되어있는지 확인
    boolean existsByUser_UserIdAndProvider(Long userId, String provider);
}
