package com.culti.mate.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;

import com.culti.auth.entity.User;
import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MatePostStatus;


public interface MateService {
	

	
//	public List<MatePost> getList(){
//		return this.mateRepository.findAll();
//	}
	
	
	// DTO -> Entity 변환 메서드
	default MatePost dtoToEntity(MatePostDTO dto, User writer, LocalDateTime eventAt) {
		MatePost entity = MatePost.builder()
				.title(dto.getTitle())
		        .category(dto.getCategory())
		        .eventAt(eventAt)
		        .location(dto.getLocation())
		        .maxPeople(dto.getMaxPeople())
		        .description(dto.getDescription())	
		        .status(MatePostStatus.OPEN)
		        .writer(writer)
				.build();		
		return entity;
	}

	// Entity -> DTO 변환 메서드
	default MatePostDTO entityToDto(MatePost entity, LocalDateTime eventAt) {

	    return MatePostDTO.builder()
	            .postId(entity.getPostId())
	            .title(entity.getTitle())
	            .category(entity.getCategory())
	            .eventAt(eventAt)
	            .location(entity.getLocation())
	            .maxPeople(entity.getMaxPeople())
	            .description(entity.getDescription())
	            .writerNickname(entity.getWriter().getNickname())
	            .build();
	}
	
	public Page<MatePost> getList(int page, int size);

	public void addPost(MatePostDTO dto, String email);


}
