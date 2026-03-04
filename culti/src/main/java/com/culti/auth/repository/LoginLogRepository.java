package com.culti.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.culti.auth.entity.LoginLog;


public interface LoginLogRepository extends JpaRepository<LoginLog, Long>{
	@Query("select l from LoginLog l join fetch l.user order by l.loginTime desc")
    List<LoginLog> findAllOrderByLoginTimeDesc();
}
