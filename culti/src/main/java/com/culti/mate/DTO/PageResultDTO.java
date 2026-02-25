package com.culti.mate.DTO;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.Data;

@Data
public class PageResultDTO<DTO, EN> {  // 제네릭 클래스
	
	private int totalPage;  // 총 페이지수
	private int page;       // 현재 페이지 번호
	private int size;       // 
	private int start, end;  // prev [1] 2... 10 next
	private boolean prev, next;	
	
	private List<Integer> pageList; // [1] 2... 10
	
	private List<DTO> dtoList;
	
	public PageResultDTO( Page<EN> result, Function<EN, DTO> fn) {
		// 1. Page<Guestbook> -> List<GuestbookDTO> 변환
		this.dtoList = result.stream()  // Stream<EN>
					      .map(fn)   // Stream<DTO>
					      .collect(Collectors.toList());  // List<DTO>
		// 2. 총 페이지 수
		this.totalPage = result.getTotalPages();
		
		// 3. 
		makePageList(result.getPageable());
	}
	
	private void makePageList(Pageable pageable) {
		this.page = pageable.getPageNumber()+1;
		this.size = pageable.getPageSize();
		int tempEnd = (int)(Math.ceil(page/10.0))*10;
		start = tempEnd - 9;
		prev = start > 1;
		end = totalPage > tempEnd  ? tempEnd : totalPage;
		next = totalPage > tempEnd;
		// 11 12 13 14 
		pageList = IntStream.rangeClosed(start, end)
				.boxed() // Stream<Integer>
				.collect(Collectors.toList()); // List<Integer>
	}

}
