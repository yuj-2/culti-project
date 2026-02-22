package com.culti.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Controller
@Log4j2
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final UserService userService;
	
	//로그인페이지
	@GetMapping("/login")
	public void login() {
		
	}
	
	//회원가입 페이지
	@GetMapping("/register")
	public void signUp() {
		
	}
	
	//회원가입 하기 (post방식)
	@PostMapping("/register")
	public String register(UserDTO userDTO, RedirectAttributes rttr) {
		log.info("👌 AuthController.register()... POST" );
		Long userId = this.userService.register(userDTO);
		//rttr.addFlashAttribute("msg", bno);
		
		return "redirect:/auth/register-complete";
	}
	
	//회원가입 완료 페이지
	@GetMapping("/register-complete")
	public void signupComplete() {
		
	}
	
	
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
	}
	
		//마이페이지
		@GetMapping("/myPage")
		public void myPage() {
			
		}
	
}
