package com.culti.support.controller;

import com.culti.support.entity.Inquiry;
import com.culti.support.service.InquiryService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;
    
 // --- 고객센터 관련 추가 페이지 ---

    // 고객센터 메인 (4개 사각형이 있는 페이지)
    @GetMapping("/support")
    public String supportMain() {
        return "support/support"; // templates/support/support.html
    }

    // 공지사항 페이지
    @GetMapping("/support/notice")
    public String noticePage() {
        return "support/notice"; 
    }

    // 예매 환불 안내 페이지
    @GetMapping("/support/refund")
    public String refundPage() {
        return "support/refund";
    }

    // FAQ 페이지
    @GetMapping("/support/faq")
    public String faqPage() {
        return "support/faq";
    }
    
    
    // ////////////////////////////////
    // 문의 페이지 구현 완료
    @GetMapping("/inquiry")
    public String inquiryPage(HttpSession session, Model model) {
        Long testUserId = 1L; 
        session.setAttribute("userId", testUserId);
        model.addAttribute("currentUserId", testUserId); 
        return "support/inquiry";
    }

    // 목록 불러오기 API
    @GetMapping("/inquiry/list")
    @ResponseBody
    public List<Inquiry> getList(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return inquiryService.getMyInquiries(userId != null ? userId : 1L);
    }

    // 상세 내용 가져오기 API
    @GetMapping("/inquiry/detail/{id}")
    @ResponseBody
    public Inquiry getDetail(@PathVariable("id") Long id) {
        return inquiryService.getInquiryDetail(id);
    }

    @PostMapping("/inquiry/save")
    @ResponseBody
    public String saveInquiry(
        @RequestParam("title") String title,
        @RequestParam("content") String content,
        HttpSession session) {
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) userId = 1L; 

        try {
            inquiryService.saveInquiry(title, content, userId);
            return "success";
        } catch (Exception e) {
            return "fail";
        }
    }
}