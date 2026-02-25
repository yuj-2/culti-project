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

@Controller
@RequiredArgsConstructor
@RequestMapping("/support")
public class InquiryController {

    private final InquiryService inquiryService;
    private final NoticeService noticeService;

    @GetMapping("")
    public String supportMain(Model model) {
        // 기존 기능 유지: 최근 공지 5개
        List<Notice> noticeList = noticeService.getLatestNotices();
        model.addAttribute("noticeList", noticeList);
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
}