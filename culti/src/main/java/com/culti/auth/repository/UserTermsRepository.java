package com.culti.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.auth.entity.UserTerms;

public interface UserTermsRepository extends JpaRepository<UserTerms, Long>{

}
