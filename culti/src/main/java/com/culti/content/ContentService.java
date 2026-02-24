package com.culti.content;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentService {

	private final ContentRepository contentRepository;
	
	// 컨텐츠 전체 목록 조회
	public List<Content> getList() {
		return this.contentRepository.findAll();
	}
	
	public Content getContent(Long id) {
		Optional<Content> content = this.contentRepository.findById(id);
		if(content.isPresent()) {
			return content.get();
		} else {
			throw new RuntimeException("content not found");
		}
	}
	
}
