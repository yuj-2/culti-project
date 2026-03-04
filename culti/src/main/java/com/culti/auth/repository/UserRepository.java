package com.culti.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	// 이메일로 회원 한 명 찾기
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUserId(Long userId);
    
 // 검색어가 있으면 이름, 이메일, 닉네임 LIKE 검색
    @Query("""
    	    SELECT u
    	    FROM User u
    	    WHERE (:keyword IS NULL OR :keyword = ''
    	           OR u.email LIKE CONCAT('%', :keyword, '%')
    	           OR u.name LIKE CONCAT('%', :keyword, '%')
    	           OR u.nickname LIKE CONCAT('%', :keyword, '%'))
    	    ORDER BY u.createdAt DESC
    	""")
    	List<User> searchUsers(@Param("keyword") String keyword);
}
