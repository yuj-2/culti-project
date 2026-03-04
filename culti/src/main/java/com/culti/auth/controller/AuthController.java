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

import com.culti.auth.dto.TermsDTO;
import com.culti.auth.dto.TermsRequestDTO;
import com.culti.auth.dto.UserDTO;
import com.culti.auth.entity.User;
import com.culti.auth.security.PrincipalDetails;
import com.culti.auth.service.SocialAuthService;
import com.culti.auth.service.TermsService;
import com.culti.auth.service.UserService;
import com.culti.booking.dto.BookingResponseDTO;
import com.culti.booking.service.BookingService;
import com.culti.mate.DTO.MateApplyMypageDTO;
import com.culti.mate.DTO.MyPostMypageDTO;
import com.culti.mate.matePage.Criteria;
import com.culti.mate.service.MateService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@Log4j2
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final MateService mateService;
    private final TermsService termsService;
    private final SocialAuthService socialAuthService;
    private final BookingService bookingService;
    
    //로그인페이지
    @GetMapping("/login")
    public String login() {
        return "/auth/login_form";
    }
    
    //회원가입 페이지
    @GetMapping("/register")
    public void signUp(Model model) {
        List<TermsDTO> termsList = this.termsService.getActiveTerms();
        model.addAttribute("termsList", termsList);
    }
    
    //회원가입 하기 (post방식)
    @PostMapping("/register")
    public String register(UserDTO userDTO, TermsRequestDTO termsRequestDTO) {
        log.info("👌 AuthController.register()... POST" );
        // 1. 유저를 등록하고 PK(ID)를 받음
        Long userId = this.userService.register(userDTO);
        
        //rttr.addFlashAttribute("msg", bno); // main 브랜치 주석 유지
        
        // 2. 중요! DB에 저장된 실제 User '엔티티'를 다시 찾아옵니다.
        // (서비스에 ID로 User를 찾는 findById 같은 메서드가 있다고 가정합니다)
        User savedUser = this.userService.getUserById(userId); 
        
        // 3. DB에서 가져온 진짜 유저 객체를 넘겨줍니다.
        this.termsService.saveUserAgreements(savedUser, termsRequestDTO.getAgreedTermIds());
        
        return "redirect:/auth/register-complete";
    }
    
    //회원가입 완료 페이지
    @GetMapping("/register-complete")
    public void signupComplete() {
        
    }
    
    /* main 브랜치 작업자의 주석 처리된 로그인 로직 유지
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
    
  //마이페이지에서 소셜로그인 연동
  	@GetMapping("/link/kakao")
  	public String linkKakao(HttpSession session, @AuthenticationPrincipal PrincipalDetails userDetails) {
  		
  		

  		 if (userDetails == null) {
  		        return "redirect:/auth/login?error=login_required";
  		    }
  		 	System.out.println("컨트롤러진입");
  		    session.setAttribute("OAUTH2_MODE", "link");
  		    session.setAttribute("LINK_USER_ID", userDetails.getUserDto().getUserId());

  		    return "redirect:/oauth2/authorization/kakao";
  	}

  	@GetMapping("/link/google")
  	public String linkGoogle(HttpSession session, @AuthenticationPrincipal PrincipalDetails userDetails) {
  		

  		if (userDetails == null) {
  	        return "redirect:/auth/login?error=login_required";
  	    }

  	    session.setAttribute("OAUTH2_MODE", "link");
  	    session.setAttribute("LINK_USER_ID", userDetails.getUserDto().getUserId());
  	    return "redirect:/oauth2/authorization/google";
  	}

  	@GetMapping("/link/naver")
  	public String linkNaver(HttpSession session,@AuthenticationPrincipal PrincipalDetails userDetails) {
  		

  		if (userDetails == null) {
  	        return "redirect:/auth/login?error=login_required";
  	    }

  	    session.setAttribute("OAUTH2_MODE", "link");
  	    session.setAttribute("LINK_USER_ID", userDetails.getUserDto().getUserId());
  	    return "redirect:/oauth2/authorization/naver";
  	}
        
    //마이페이지
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/myPage")
    public void myPage(@AuthenticationPrincipal PrincipalDetails userDetails, Model model
            , @RequestParam(name="reservationTab", defaultValue="booking") String reservationTab
            , @RequestParam(name="mateTab", defaultValue="received") String mateTab,
            @RequestParam(name="mateSection", defaultValue="mate") String mateSection,
            @RequestParam(name="pageMyPosts", defaultValue="1") int pageMyPosts,
            @RequestParam(name="pageApplied", defaultValue="1") int pageApplied,
            @RequestParam(name="pageReceived", defaultValue="1") int pageReceived,
            @RequestParam(name="size", defaultValue="9") int size,
            Criteria criteria){
        
        String email=userDetails.getUsername();
        UserDTO userDTO=userDetails.getUserDto(); // feature/user 로직 기준
        model.addAttribute("user",userDTO);
        
        //소셜로그인 연동 여부를 model로 넘김
        boolean kakao=this.socialAuthService
                .existsByUser_UserIdAndProvider(userDTO.getUserId(), "kakao");
        
        boolean google=this.socialAuthService
                .existsByUser_UserIdAndProvider(userDTO.getUserId(), "google");
        
        boolean naver=this.socialAuthService
                .existsByUser_UserIdAndProvider(userDTO.getUserId(), "naver");
        
        model.addAttribute("kakao",kakao);
        model.addAttribute("google",google);
        model.addAttribute("naver",naver);
        
        // 예매 내역 조회
        List<BookingResponseDTO> bookings = bookingService.getMyBookings(userDTO.getUserId());
        model.addAttribute("bookings", bookings);
        
        // 취소/환불 내역 조회
        List<BookingResponseDTO> cancelledBookings = bookingService.getCancelledBookings(userDTO.getUserId());
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
    
    //      =================동행매칭======================
    
    // 수락
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/accept")
    public String accept(@PathVariable("id") Long applyId,
                         @AuthenticationPrincipal PrincipalDetails userDetails) {
        mateService.accept(applyId, userDetails.getUsername());
        return "redirect:/auth/myPage?mateSection=mate&mateTab=received";
    }

    // 거절
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable("id") Long applyId,
                         @AuthenticationPrincipal PrincipalDetails userDetails) {
        mateService.reject(applyId, userDetails.getUsername());
        return "redirect:/auth/myPage?mateSection=mate&mateTab=received";
    }

    // 취소(내가 신청한 것)
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable("id") Long applyId,
                         @AuthenticationPrincipal PrincipalDetails userDetails) {
        mateService.cancel(applyId, userDetails.getUsername());
        return "redirect:/auth/myPage?mateSection=mate&mateTab=sent";
    }
    //      =================동행매칭====================== 
    
    // 예매 취소
    @PostMapping("/booking/cancel")
    public String cancelBooking(@RequestParam("bookingNumber") String bookingNumber){
        bookingService.cancelBooking(bookingNumber);
        return "redirect:/auth/myPage?reservationTab=cancel";
    }
    
}