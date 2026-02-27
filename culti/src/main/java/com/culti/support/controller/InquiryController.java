package com.culti.support.controller;

import com.culti.support.entity.Notice; 
import com.culti.support.entity.Inquiry;
import com.culti.support.service.InquiryService;
import com.culti.support.service.NoticeService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.culti.support.entity.Faq;
import com.culti.support.service.FaqService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/support")
public class InquiryController {

    private final InquiryService inquiryService;
    private final NoticeService noticeService;
    private final FaqService faqService; // FAQ 서비스 추가

    @GetMapping("")
    public String supportMain(Model model, 
        // 기본값을 5개씩, ID 내림차순으로 설정
        @PageableDefault(size = 5, sort = "noticeId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // 1. 기존 List 대신 Page 객체로 가져옵니다.
        Page<Notice> noticePage = noticeService.getNoticeList(pageable);
        
        // 2. HTML에서 페이징 처리를 할 수 있도록 Page 객체 자체를 넘깁니다.
        model.addAttribute("noticePage", noticePage);
        
        // 기존 List 이름도 유지하고 싶다면 content만 따로 담아줘도 됩니다.
        model.addAttribute("noticeList", noticePage.getContent()); 
        
        return "support/support";
    }

    // 1. 공지사항 전체 목록 (페이징 적용)
    @GetMapping("/notice")
    public String noticeListPage(Model model, 
        @PageableDefault(size = 10, sort = "noticeId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Notice> noticePage = noticeService.getNoticeList(pageable);
        model.addAttribute("noticePage", noticePage);
        return "support/notice"; // templates/support/notice.html
    }

    // 2. 공지사항 상세보기 (ID로 조회)
    @GetMapping("/notice/view")
    public String noticeDetail(@RequestParam("id") Long id, Model model) {
        Notice notice = noticeService.getNoticeDetail(id);
        model.addAttribute("notice", notice);
        return "support/noticeDetail"; // templates/support/noticeDetail.html
    }

    @GetMapping("/inquiry")
    public String inquiryPage(HttpSession session, Model model) {
        session.setAttribute("userId", 1L); 
        model.addAttribute("currentUserId", 1L);
        model.addAttribute("inquiry", new Inquiry()); 
        return "support/inquiry";
    }

    @GetMapping("/inquiry/list")
    @ResponseBody
    public List<Inquiry> getList(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return inquiryService.getMyInquiries(userId != null ? userId : 1L);
    }

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
            Inquiry inquiry = Inquiry.builder()
                    .userId(userId)
                    .inquiryTitle(title)
                    .inquiryContent(content)
                    .build();
            
            inquiryService.saveInquiry(inquiry);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }
    
    
    // FAQ 목록 메서드 수정 (페이징 + 카테고리 필터 추가)
    @GetMapping("/faq")
    public String faqList(Model model, 
        @RequestParam(value = "category", required = false) String category,
        @PageableDefault(size = 10, sort = "faqId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Faq> faqPage;
        if (category != null && !category.isEmpty() && !category.equals("전체")) {
            faqPage = faqService.getFaqListByCategory(category, pageable);
        } else {
            faqPage = faqService.getFaqList(pageable);
        }
        
        model.addAttribute("faqPage", faqPage);
        model.addAttribute("currentCategory", category != null ? category : "전체");
        // 카테고리 목록 (탭 생성을 위함)
        model.addAttribute("categories", List.of("전체", "예매", "전시", "취소/환불", "동행", "회원"));
        
        return "support/faq";
    }
    
    
    // 환불 안내 페이지 매핑
    @GetMapping("/refund")
    public String refundInfo() {
        return "support/refund"; 
    }
    
}