package com.culti.mate.DTO;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class PageRequestDTO {
	
	private int page; // 현재 페이지 번호
	private int size; // 한 페이지에 출력할 수
	
	// 검색 추가
	private String type;    // 검색조건
	private String keyword; // 검색어
	
	public PageRequestDTO() {
		this.page = 1;
		this.size = 9;
	}
	
	public Pageable getPageable(Sort sort) {
		return PageRequest.of(this.page-1, this.size, sort);
	}

}
