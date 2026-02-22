package com.culti.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
	
	// 이메일로 회원 한 명 찾기
    Optional<User> findByEmail(String email);
}
