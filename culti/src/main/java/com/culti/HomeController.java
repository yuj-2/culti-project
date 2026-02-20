package com.culti;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

	@GetMapping("/home")
	public void home() {
		
	}
	
	@GetMapping("/reservation")
	public String subpage() {
		return "reservation/reservation"; 
	}
	
	@GetMapping("/calendar")
	public String calendar() {
	    return "calendar/calendar";
	}
	
}
