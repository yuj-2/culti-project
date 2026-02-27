package com.culti.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.auth.entity.EmailVerification;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long>{
	
}
