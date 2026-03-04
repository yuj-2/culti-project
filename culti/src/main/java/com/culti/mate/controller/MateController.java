package com.culti.mate.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.service.UserService;
import com.culti.mate.DTO.MateCommentDTO;
import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MateApplyStatus;
import com.culti.mate.matePage.Criteria;
import com.culti.mate.matePage.PageDTO;
import com.culti.mate.repository.MateRepository;
import com.culti.mate.service.MateService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/mate")
@RequiredArgsConstructor
public class MateController {
	
	private final MateService mateService; 
	private final UserService userService;
	private final MateRepository mateRepository;

	@GetMapping("/mate")
	public String mate(Model model
			, @AuthenticationPrincipal UserDetails userDetails
			, @RequestParam(value = "page", defaultValue = "1") int page
			, @RequestParam(value = "size", defaultValue = "9") int size
			, @RequestParam(value="category", defaultValue="all") String category
			) throws Exception {
		 List<Long> appliedPostIds = Collections.emptyList();
		
		 Map<Long, MateApplyStatus> appliedStatusMap = Collections.emptyMap();
		 
		if (userDetails != null) {
	        String email = userDetails.getUsername();
	        UserDTO userDTO = userService.findByEmail(email);
	        model.addAttribute("user", userDTO);
	        
	        appliedPostIds = mateService.getAppliedPostIds(email);
	        appliedStatusMap = mateService.getAppliedStatusMap(email);
	    }
		
		Page<MatePost> paging;
	    if ("all".equalsIgnoreCase(category)) {
	        paging = mateService.getList(page, size);
	    } else {
	        paging = mateService.getListByCategory(category, page, size);
	    }
	    
	    Map<Long, Long> acceptedCountMap = mateService.getAcceptedCountMap(paging);
	    
		model.addAttribute("paging", paging);
		model.addAttribute("category", category);
		model.addAttribute("appliedPostIds", appliedPostIds);
		model.addAttribute("appliedStatusMap", appliedStatusMap);
		model.addAttribute("acceptedCountMap", acceptedCountMap);
		model.addAttribute("appliedStatusMapJson", new ObjectMapper().writeValueAsString(appliedStatusMap));
	    
		
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
		
		LocalDate date = LocalDate.parse(dto.getDate());
	    if (date != null && date.isBefore(LocalDate.now())) {
	        throw new IllegalArgumentException("과거 날짜는 선택할 수 없습니다.");
	    }
		String email = userDetails.getUsername();
		mateService.addPost(dto, email); // writer 세팅 포함해서 저장
		
		return "redirect:/mate/mate";
	}
	
	// 게시글 삭제
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/deletePost")
	public String deletePost(@RequestParam("postId") Long postId,
	                         @AuthenticationPrincipal UserDetails userDetails,
	                         HttpServletRequest request) {

	    mateService.delete(postId, userDetails.getUsername());

	    String referer = request.getHeader("Referer");

	    // 마이페이지에서 삭제한 경우 → 마이페이지 "내 게시글" 탭으로 강제 복귀
	    if (referer != null && referer.contains("/auth/myPage")) {
	        return "redirect:/auth/myPage?mateSection=mate&mateTab=myPosts";
	    }

	    // 그 외(게시판에서 삭제) → 원래 페이지로
	    return "redirect:" + (referer != null ? referer : "/mate/mate");
	}
	
	// 참여신청
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/applyPost")
	public String applyPost(@RequestParam("postId") Long postId,
            @RequestParam("message") String message,
            @AuthenticationPrincipal UserDetails userDetails) {
		String email = userDetails.getUsername();
		mateService.apply(postId, email, message);
		return "redirect:/mate/mate";
	}
//	
//	@PreAuthorize("isAuthenticated()")
//	@PostMapping("/addComment")
//	public String addComment(@RequestParam("postId") Long postId,
//	                         @RequestParam("comment") String comment,
//	                         @AuthenticationPrincipal UserDetails userDetails) {
//	    String email = userDetails.getUsername();
//	    mateService.addComment(postId, email, comment);
//	    return "redirect:/mate/mate";
//	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/updateComment")
	public String updateComment(@RequestParam("commentId") Long commentId,
            @RequestParam("comment") String comment,
            @AuthenticationPrincipal UserDetails userDetails) {
	    String email = userDetails.getUsername();
	    mateService.updateComment(commentId, email, comment);
	    return "redirect:/mate/mate";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/deleteComment")
	public String deleteComment(@RequestParam("commentId") Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
	    String email = userDetails.getUsername();
	    mateService.deleteComment(commentId, email);
	    return "redirect:/mate/mate";
	}
	
	@GetMapping("/open/{postId}")
	public String openPost(@PathVariable("postId") Long postId,
            @RequestParam(value = "size", defaultValue = "10") int size) {

	    MatePost post = mateRepository.findById(postId)
	        .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

	    // “all” 탭 기준으로 열고 싶으면 categoryOrNull = null
	    int pageAll = mateService.calcPageForPost(postId, size, null);

	    return "redirect:/mate/mate?category=all&page=" + pageAll + "&size=" + size + "&openPostId=" + postId;
	}
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/addComment")
	@ResponseBody
	public ResponseEntity<MateCommentDTO> addComment(@RequestBody Map<String, Object> data,
	                                     @AuthenticationPrincipal UserDetails user) {
	    Long postId = Long.valueOf(data.get("postId").toString());
	    String content = data.get("content").toString();

	    MateCommentDTO dto = mateService.addComment(postId, user.getUsername(), content);
	    return ResponseEntity.ok(dto);
	}
	

	@GetMapping("/comments")
	@ResponseBody
	public List<MateCommentDTO> getComments(@RequestParam("postId") Long postId) {
	    return mateService.getComments(postId);
	}
	
}
