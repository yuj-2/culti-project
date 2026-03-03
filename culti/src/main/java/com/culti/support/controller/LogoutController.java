package com.culti.support.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LogoutController {

    @GetMapping("/admin/logout") // 경로를 시큐리티 기본값과 겹치지 않게 변경
    public String adminLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 무효화 (로그아웃 처리)
        }
        return "redirect:/home"; // localhost/home으로 이동
    }
}