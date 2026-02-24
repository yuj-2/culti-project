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

@Controller
@RequiredArgsConstructor
@RequestMapping("/support")
public class InquiryController {

    private final InquiryService inquiryService;
    private final NoticeService noticeService;

    @GetMapping("")
    public String supportMain(Model model) {
        List<Notice> noticeList = noticeService.getLatestNotices();
        model.addAttribute("noticeList", noticeList);
        return "support/support";
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