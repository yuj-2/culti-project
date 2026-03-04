	package com.culti.auth.controller;
	
	import java.util.List;
	
	import org.springframework.data.domain.Page;
	import org.springframework.security.access.prepost.PreAuthorize;
	import org.springframework.security.core.annotation.AuthenticationPrincipal;
	import org.springframework.security.core.userdetails.UserDetails;
	import org.springframework.stereotype.Controller;
	import org.springframework.ui.Model;
	import org.springframework.web.bind.annotation.GetMapping;
	import org.springframework.web.bind.annotation.PathVariable;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RequestParam;
	
	import com.culti.auth.dto.UserDTO;
	import com.culti.auth.service.UserService;
	import com.culti.booking.dto.BookingResponseDTO;
	import com.culti.booking.service.BookingService;
	import com.culti.mate.DTO.MateApplyMypageDTO;
	import com.culti.mate.DTO.MyPostMypageDTO;
	import com.culti.mate.matePage.Criteria;
	import com.culti.mate.service.MateService;
	
	import lombok.RequiredArgsConstructor;
	import lombok.extern.log4j.Log4j2;
	
	
	@Controller
	@Log4j2
	@RequestMapping("/auth")
	@RequiredArgsConstructor
	public class AuthController {
		
		private final UserService userService;
		private final MateService mateService;
		private final BookingService bookingService;
		
		//로그인페이지
		@GetMapping("/login")
		public String login() {
			return "/auth/login_form";
		}
		
		//회원가입 페이지
		@GetMapping("/register")
		public void signUp() {
			
		}
		
		//회원가입 하기 (post방식)
		@PostMapping("/register")
		public String register(UserDTO userDTO) {
			log.info("👌 AuthController.register()... POST" );
			Long userId = this.userService.register(userDTO);
			//rttr.addFlashAttribute("msg", bno);
			
			return "redirect:/auth/register-complete";
		}
		
		//회원가입 완료 페이지
		@GetMapping("/register-complete")
		public void signupComplete() {
			
		}
		
		/*
		@PostMapping("/login")
		public String login(@RequestParam("email") String email,@RequestParam("password") String password, HttpSession session, RedirectAttributes rttr) {
		    UserDTO loginUser = userService.login(email, password);
	
		    if (loginUser != null) {
		        // 로그인 성공: 세션에 사용자 정보 저장 후 home(임시)으로 이동
		        session.setAttribute("user", loginUser);
		        return "redirect:/home";
		    } else {
		        // 로그인 실패: 메시지 들고 로그인 페이지로 다시이동
		        rttr.addFlashAttribute("msg", "아이디 또는 비밀번호가 틀렸습니다.");
		        
		        return "redirect:/auth/login";
		    }
		}*/
			
			//마이페이지
			@PreAuthorize("isAuthenticated()")
			@GetMapping("/myPage")
			public void myPage(@AuthenticationPrincipal UserDetails userDetails, Model model
					, @RequestParam(name="reservationTab", defaultValue="booking") String reservationTab
										, @RequestParam(name="mateTab", defaultValue="received") String mateTab,
									    @RequestParam(name="mateSection", defaultValue="mate") String mateSection,
									    @RequestParam(name="pageMyPosts", defaultValue="1") int pageMyPosts,
									    @RequestParam(name="pageApplied", defaultValue="1") int pageApplied,
									    @RequestParam(name="pageReceived", defaultValue="1") int pageReceived,
									    @RequestParam(name="size", defaultValue="9") int size,
									    Criteria criteria){
				
				String email=userDetails.getUsername();
				UserDTO userDTO=this.userService.findByEmail(email);
				model.addAttribute("user",userDTO);
				
				// 예매 내역 조회
				List<BookingResponseDTO> bookings =
				        bookingService.getMyBookings(userDTO.getUserId());
	
				model.addAttribute("bookings", bookings);
				
				// 취소/환불 내역 조회
				List<BookingResponseDTO> cancelledBookings =
				        bookingService.getCancelledBookings(userDTO.getUserId());
	
				model.addAttribute("cancelledBookings", cancelledBookings);
	
				// 추가
				model.addAttribute("reservationTab", reservationTab);
				
				
				
				
				// ===== 동행매칭==================
				// 탭 상태
			    model.addAttribute("mateTab", mateTab);
			    model.addAttribute("mateSection", mateSection);
	
			    // 탭별 페이징 데이터 (3개 Page로 통일)
			    Page<MyPostMypageDTO> myPostsPaging = mateService.getMyPostsDto(email, pageMyPosts, size);
			    model.addAttribute("myPostsPaging", myPostsPaging);
			    Page<MateApplyMypageDTO> appliedPaging = mateService.getMyApplied(email, pageApplied, size);
			    Page<MateApplyMypageDTO> receivedPaging = mateService.getReceivedApplies(email, pageReceived, size);
	
			    model.addAttribute("appliedPaging", appliedPaging);
			    model.addAttribute("receivedPaging", receivedPaging);
	
			    // 현재 페이지 값 (링크 만들 때 유지)
			    model.addAttribute("pageMyPosts", pageMyPosts);
			    model.addAttribute("pageApplied", pageApplied);
			    model.addAttribute("pageReceived", pageReceived);
			    model.addAttribute("size", size);
			    // ====동행매칭=====================
			    
	
			    
				
			}
			
			@GetMapping("/find-password")
			public void findPassword() {
				
			}
			
			
	//		=================동행매칭======================
			
			// 수락
		    @PreAuthorize("isAuthenticated()")
		    @PostMapping("/{id}/accept")
		    public String accept(@PathVariable("id") Long applyId,
		                         @AuthenticationPrincipal UserDetails userDetails) {
		        mateService.accept(applyId, userDetails.getUsername());
		        return "redirect:/auth/myPage?mateSection=mate&mateTab=received";
		    }
	
		    // 거절
		    @PreAuthorize("isAuthenticated()")
		    @PostMapping("/{id}/reject")
		    public String reject(@PathVariable("id") Long applyId,
		                         @AuthenticationPrincipal UserDetails userDetails) {
		        mateService.reject(applyId, userDetails.getUsername());
		        return "redirect:/auth/myPage?mateSection=mate&mateTab=received";
		    }
	
		    // 취소(내가 신청한 것)
		    @PreAuthorize("isAuthenticated()")
		    @PostMapping("/{id}/cancel")
		    public String cancel(@PathVariable("id") Long applyId,
		                         @AuthenticationPrincipal UserDetails userDetails) {
		        mateService.cancel(applyId, userDetails.getUsername());
		        return "redirect:/auth/myPage?mateSection=mate&mateTab=sent";
		    }
	//		=================동행매칭======================	 
		    // 예매 취소
		    @PostMapping("/booking/cancel")
		    public String cancelBooking(@RequestParam("bookingNumber") String bookingNumber){
	
		        bookingService.cancelBooking(bookingNumber);
	
		        return "redirect:/auth/myPage?reservationTab=cancel";
		    }
		
	}
