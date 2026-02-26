package com.culti.mate.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.entity.MatePost;
import com.culti.mate.repository.MateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MateServiceImpl implements MateService {
	
	private final MateRepository mateRepository;
	
	private final UserRepository userRepository;

	public Page<MatePost> getList(int page, int size){
		//                                  0(1번페이지)
		Pageable pageable = PageRequest.of(page-1, size, Sort.by("postId").descending());
		return this.mateRepository.findAll(pageable);
	}
	
	public void addPost(MatePostDTO dto, String email) {
	    User writer = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + email));

	    LocalDateTime eventAt = LocalDateTime.of(
	            LocalDate.parse(dto.getDate()),
	            LocalTime.parse(dto.getTime())
	    );

	    dto.setEventAt(eventAt); // ★ 이 한 줄이 핵심 (DTO가 @Data라 setter 있음)

	    MatePost entity = this.dtoToEntity(dto, writer, eventAt);
	    mateRepository.save(entity);
	}

	
}
