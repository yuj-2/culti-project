package com.culti.mate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mate")
@RequiredArgsConstructor
public class mateController {

	@GetMapping("/mate")
	public void home() {
		
	}
		
	
}
