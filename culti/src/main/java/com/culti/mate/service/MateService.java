package com.culti.mate.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MatePostCategory;
import com.culti.mate.enums.MatePostStatus;
import com.culti.mate.repository.MateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MateService {

	private final MateRepository mateRepository;
	
//	public List<MatePost> getList(){
//		return this.mateRepository.findAll();
//	}
	
	public Page<MatePost> getList(int page, int size){
		//                                  0(1번페이지)
		Pageable pageable = PageRequest.of(page-1, size, Sort.by("postId").descending());
		return this.mateRepository.findAll(pageable);
	}
	
	public void addPost(String title, String category, LocalDateTime eventAt, String location
				, Integer maxPeople, String nickname, String description ) {
		MatePost post = MatePost.builder()
	            .title(title)
	            .category(MatePostCategory.valueOf(category.toUpperCase()))
	            .eventAt(eventAt)
	            .location(location)
	            .maxPeople(maxPeople)
	            .description(description)
	            .status(MatePostStatus.OPEN)  // enum 값에 맞게
	            .build();

	    mateRepository.save(post);
		
	
	}
	
}
