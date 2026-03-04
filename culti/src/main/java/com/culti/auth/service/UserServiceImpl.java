package com.culti.auth.service;


import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.booking.controller.BookingController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	
	@Override
	public Long register(UserDTO userDTO) {
		log.info("😝 UserServiceImpl.register()..." + userDTO);
		userDTO.setRole("USER");
		userDTO.setStatus("정상");
		// BoardDTO -> Board 엔티티로 변환
		User entity = this.dtoToEntity(userDTO);
		
		entity.setPassword(this.passwordEncoder.encode(entity.getPassword()));
		
		this.userRepository.save(entity);
		
		return entity.getUserId(); // 가입된 회원 고유Id 번호
	}

	@Override
	public UserDTO login(String email, String password) {
		// 1. DB에서 이메일로 회원 조회
	    Optional<User> result = userRepository.findByEmail(email);

	    if (result.isPresent()) {
	        User user = result.get();
	        // 2. 비밀번호가 일치하는지 확인 (현재는 암호화 전 생짜 비교)
	        if (user.getPassword().equals(password)) {
	            // 로그인 성공 시 엔티티를 DTO로 변환해서 반환
	            return entityToDto(user);
	        }
	    }
	    
	    // 3. 회원이 없거나 비밀번호가 틀리면 null 반환
	    return null;
	}

	@Override
	public UserDTO findByEmail(String email) {
		// 1. DB에서 이메일로 회원 조회
	    Optional<User> result = userRepository.findByEmail(email);

	    if (result.isPresent()) {
	        User user = result.get();
	        return entityToDto(user);
	        
	    }
	    
	    return null;
	}

	@Override
	public User findEntityByEmail(String email) {
		// 1. DB에서 이메일로 회원 조회
	    Optional<User> result = userRepository.findByEmail(email);

	    if (result.isPresent()) {
	        User user = result.get();
	        return user;
	        
	    }
	    
	    return null;
	}

	@Override
	public User getUserById(Long userID) {
		Optional<User> result = this.userRepository.findByUserId(userID);

	    if (result.isPresent()) {
	        User user = result.get();
	        return user;
	        
	    }
	    
	    return null;
	}

	@Override
	public void saveUser(User user) {
		this.userRepository.save(user);
		
	}

	@Transactional
	@Override
	public User changePassword(String email, String currentPassword, String newPassword) {
		Optional<User> result = userRepository.findByEmail(email);
        User user=null;  
        if (result.isEmpty()) {
			throw new RuntimeException("사용자 없음");
		}else {
			user=result.get();
		}  

        // 1. 현재 비밀번호 검증
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 2. 새 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(newPassword);

        // 3. 저장
        user.setPassword(encodedPassword);
        
        return user;
		
	}

}
