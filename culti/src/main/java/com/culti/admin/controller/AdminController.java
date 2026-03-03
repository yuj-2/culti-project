package com.culti.admin.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.culti.admin.dto.ContentFormDTO;
import com.culti.admin.service.AdminService;

import groovy.util.logging.Log4j;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Log4j2
public class AdminController {

	private final AdminService adminService;
	
	@GetMapping("/main")
	public void main() {
		
	}
	
	// 콘텐츠 목록 등록 경로
	@GetMapping("/content/new")
    public String contentForm() {
        return "admin/content-form";
    }
	
	@PostMapping("/content/new")
	public String registerContent(@ModelAttribute ContentFormDTO formDTO) {
	    try {
	        adminService.registerContent(formDTO);
	        
	    } catch (Exception e) {
	        log.error("콘텐츠 등록 중 에러 발생!", e);
	        
	        return "redirect:/admin/content/new?error"; 
	    }
	    
	    return "redirect:/admin/main";
	}
	
}
