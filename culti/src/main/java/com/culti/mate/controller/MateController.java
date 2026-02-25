package com.culti.mate.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.culti.mate.entity.MatePost;
import com.culti.mate.matePage.Criteria;
import com.culti.mate.matePage.PageDTO;
import com.culti.mate.service.MateService;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/mate")
@RequiredArgsConstructor
public class MateController {
	
	private final MateService mateService; 

	@GetMapping("/mate")
	public String mate(Model model
			, @RequestParam(value = "page", defaultValue = "1") int page
			, @RequestParam(value = "size", defaultValue = "9") int size
			) {
		Page<MatePost> paging = this.mateService.getList(page, size);
		model.addAttribute("paging", paging);
		
		// [1] 2 3 4 5 6 7 8 9 10 >
		Criteria criteria = new Criteria(page, size);
		int total = (int) paging.getTotalElements(); // 총 레코드 수
		model.addAttribute("pageMaker", new PageDTO(criteria, total)  ) ;
		return "mate/mate";
	}
	
	@PostMapping("/add")
	public String addPost(@RequestBody String entity) {
		//TODO: process POST request
		
		return entity;
	}
	
	
	
	
		
//	마이페이지로 옮겨야 함
	@GetMapping("/mateMypage")
	public void mateMypage() {
		
	}
	
}
