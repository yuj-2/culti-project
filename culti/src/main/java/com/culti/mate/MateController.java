package com.culti.mate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mate")
@RequiredArgsConstructor
public class MateController {

	@GetMapping("/mate")
	public String mate() {
		return "mate/mate"; 
	}
		
//	마이페이지로 옮겨야 함
	@GetMapping("/mateMypage")
	public void mateMypage() {
		
	}
	
}
