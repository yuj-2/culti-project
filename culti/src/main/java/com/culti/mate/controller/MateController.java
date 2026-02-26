package com.culti.mate.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.service.UserService;
import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.entity.MatePost;
import com.culti.mate.matePage.Criteria;
import com.culti.mate.matePage.PageDTO;
import com.culti.mate.service.MateService;
import com.culti.mate.service.MateServiceImpl;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/mate")
@RequiredArgsConstructor
public class MateController {
	
	private final MateService mateService; 
	private final UserService userService;

	@GetMapping("/mate")
	public String mate(Model model
			, @AuthenticationPrincipal UserDetails userDetails
			, @RequestParam(value = "page", defaultValue = "1") int page
			, @RequestParam(value = "size", defaultValue = "9") int size
			) {
		if (userDetails != null) {
	        String email = userDetails.getUsername();
	        UserDTO userDTO = userService.findByEmail(email);
	        model.addAttribute("user", userDTO);
	    }
		
		Page<MatePost> paging = this.mateService.getList(page, size);
		model.addAttribute("paging", paging);
		
		// [1] 2 3 4 5 6 7 8 9 10 >
		Criteria criteria = new Criteria(page, size);
		int total = (int) paging.getTotalElements(); // 총 레코드 수
		model.addAttribute("pageMaker", new PageDTO(criteria, total)  ) ;
		return "mate/mate";
	}
	
	
	// 새 게시글 추가
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/addPost")
	public String addPost(@ModelAttribute MatePostDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
		
		String email = userDetails.getUsername();
		mateService.addPost(dto, email); // writer 세팅 포함해서 저장
		
		return "redirect:/mate/mate";
	}
	
	// 게시글 삭제
	@PostMapping("/deletePost")
	public String deletePost(@RequestBody String entity) {
		//TODO: process POST request
		
		return entity;
	}
	
	// 참여신청
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/applyPost")
	public String apply(@RequestBody String entity) {
		//TODO: process POST request
		
		return entity;
	}
	
	// 댓글 쓰기
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/addComment")
	public String addComment(@RequestBody String entity) {
		//TODO: process POST request
		
		return entity;
	}
	
	// 댓글 수정
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/updateComment")
	public String updateComment(@RequestBody String entity) {
		//TODO: process POST request
		
		return entity;
	}
	

	
	// 댓글 삭제
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/deleteComment")
	public String deleteComment(@RequestBody String entity) {
		//TODO: process POST request
		
		return entity;
	}
	
	
	
	
	
		
//	마이페이지로 옮겨야 함
	@GetMapping("/mateMypage")
	public void mateMypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		String email=userDetails.getUsername();
		UserDTO userDTO=this.userService.findByEmail(email);
		model.addAttribute("user",userDTO);
	}
	
}
