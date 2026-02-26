package com.culti.mate.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.service.UserService;
import com.culti.mate.DTO.MatePostDTO;
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
	private final UserService userService;

	@GetMapping("/mate")
	public String mate(Model model
			, @AuthenticationPrincipal UserDetails userDetails
			, @RequestParam(value = "page", defaultValue = "1") int page
			, @RequestParam(value = "size", defaultValue = "9") int size
			, @RequestParam(value="category", defaultValue="all") String category
			) {
		if (userDetails != null) {
	        String email = userDetails.getUsername();
	        UserDTO userDTO = userService.findByEmail(email);
	        model.addAttribute("user", userDTO);
	    }
		
		Page<MatePost> paging;
	    if ("all".equalsIgnoreCase(category)) {
	        paging = mateService.getList(page, size);
	    } else {
	        paging = mateService.getListByCategory(category, page, size);
	    }
		model.addAttribute("paging", paging);
		model.addAttribute("category", category);
		
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
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/deletePost")
	public String deletePost(@RequestParam("postId") Long postId
										,@AuthenticationPrincipal UserDetails userDetails) {
		String email = userDetails.getUsername();
		this.mateService.delete(postId, email);
		
		return "redirect:/mate/mate";
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
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/addComment")
	public String addComment(@RequestParam("postId") Long postId,
	                         @RequestParam("comment") String comment,
	                         @AuthenticationPrincipal UserDetails userDetails) {
	    String email = userDetails.getUsername();
	    mateService.addComment(postId, email, comment);
	    return "redirect:/mate/mate";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/updateComment")
	public String updateComment(@RequestParam Long commentId,
								@RequestParam("comment") String comment,
	                            @AuthenticationPrincipal UserDetails userDetails) {
	    String email = userDetails.getUsername();
	    mateService.updateComment(commentId, email, comment);
	    return "redirect:/mate/mate";
	}

	@PreAuthorize("isAuthenticated()")
	@PostMapping("/deleteComment")
	public String deleteComment(@RequestParam Long commentId,
	                            @AuthenticationPrincipal UserDetails userDetails) {
	    String email = userDetails.getUsername();
	    mateService.deleteComment(commentId, email);
	    return "redirect:/mate/mate";
	}
	
	
	
	
	
		
//	마이페이지로 옮겨야 함
	@GetMapping("/mateMypage")
	public void mateMypage(@RequestParam(name="tab", defaultValue="received") String tab,
							        @RequestParam(name="section", defaultValue="mate") String section,
							        @AuthenticationPrincipal UserDetails userDetails,
							        Model model) {
		String email=userDetails.getUsername();
		UserDTO userDTO=this.userService.findByEmail(email);
		model.addAttribute("user",userDTO);
		model.addAttribute("tab", tab);
		model.addAttribute("section", section);

	    // DB 조회해서 담기
	    model.addAttribute("receivedApps", mateService.getReceivedApps(email));
	    model.addAttribute("sentApps", mateService.getSentApps(email));
	}
	
	// 수락
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/accept")
    public String accept(@PathVariable("id") Long applyId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        mateService.accept(applyId, userDetails.getUsername());
        return "redirect:/mate/mateMypage?section=mate&tab=received";
    }

    // 거절
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable("id") Long applyId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        mateService.reject(applyId, userDetails.getUsername());
        return "redirect:/mate/mateMypage?section=mate&tab=received";
    }

    // 취소(내가 신청한 것)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long applyId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        mateService.cancel(applyId, userDetails.getUsername());
        return "redirect:/mate/mateMypage?section=mate&tab=sent";
    }
	
}
