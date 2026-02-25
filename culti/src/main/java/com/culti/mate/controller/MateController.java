package com.culti.mate.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.culti.mate.entity.MatePost;
import com.culti.mate.service.MateService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mate")
@RequiredArgsConstructor
public class MateController {
	
	private final MateService mateService; 

	@GetMapping("/mate")
	public String mate(Model model) {
		List<MatePost> posts = mateService.getList();
		model.addAttribute("posts", posts);
		return "mate/mate"; 
	}
		
//	마이페이지로 옮겨야 함
	@GetMapping("/mateMypage")
	public void mateMypage() {
		
	}
	
}
