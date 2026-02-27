package com.culti.support.controller;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.service.UserService; // 팀원의 유저 서비스
import com.culti.support.entity.Faq;
import com.culti.support.entity.Inquiry;
import com.culti.support.entity.Notice;
import com.culti.support.service.FaqService;
import com.culti.support.service.InquiryService;
import com.culti.support.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/support")
public class InquiryController {

    private final InquiryService inquiryService;
    private final NoticeService noticeService;
    private final FaqService faqService;
    private final UserService userService; // 팀원의 서비스 주입

    // [고객센터 메인]
    @GetMapping("")
    public String supportMain(Model model, 
        @PageableDefault(size = 5, sort = "noticeId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Notice> noticePage = noticeService.getNoticeList(pageable);
        model.addAttribute("noticePage", noticePage);
        model.addAttribute("noticeList", noticePage.getContent()); 
        
        return "support/support";
    }

    // [1. 공지사항 목록]
    @GetMapping("/notice")
    public String noticeListPage(Model model, 
        @PageableDefault(size = 10, sort = "noticeId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Notice> noticePage = noticeService.getNoticeList(pageable);
        model.addAttribute("noticePage", noticePage);
        return "support/notice";
    }

    // [2. 공지사항 상세보기]
    @GetMapping("/notice/view")
    public String noticeDetail(@RequestParam("id") Long id, Model model) {
        Notice notice = noticeService.getNoticeDetail(id);
        model.addAttribute("notice", notice);
        return "support/noticeDetail";
    }

 // [3. 1:1 문의 페이지 접속]
    @GetMapping("/inquiry")
    public String inquiryPage(Principal principal, Model model) {
        // 로그인 체크 후 유저 정보 전달
        if (principal != null) {
            // 팀원이 알려준 로직 적용
            String email = principal.getName();
            UserDTO userDTO = this.userService.findByEmail(email);
            
            model.addAttribute("user", userDTO); // HTML에서 ${user.nickname} 등으로 사용 가능
            model.addAttribute("currentUserId", userDTO.getUserId()); // 기존 로직 유지
        }
        
        model.addAttribute("inquiry", new Inquiry()); 
        return "support/inquiry";
    }

    // [4. 문의 목록 가져오기 (비동기)]
    @GetMapping("/inquiry/list")
    @ResponseBody
    public List<Inquiry> getList(Principal principal) {
        if (principal == null) return null;

        // 로그인한 이메일로 유저를 찾아 userId 획득
        UserDTO user = userService.findByEmail(principal.getName());
        return inquiryService.getMyInquiries(user.getUserId());
    }

    // [5. 문의 상세 보기 (비동기)]
    @GetMapping("/inquiry/detail/{id}")
    @ResponseBody
    public Inquiry getDetail(@PathVariable("id") Long id) {
        return inquiryService.getInquiryDetail(id);
    }

    // [6. 문의 저장 (비동기)]
    @PostMapping("/inquiry/save")
    @ResponseBody
    public String saveInquiry(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            Principal principal) {
        
        if (principal == null) return "fail";

        try {
            // 로그인한 이메일로 유저를 찾아 userId 획득
            UserDTO user = userService.findByEmail(principal.getName());

            Inquiry inquiry = Inquiry.builder()
                    .userId(user.getUserId()) // 실시간 로그인 유저 ID
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

    // [7. FAQ 목록 (페이징 + 카테고리)]
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
        model.addAttribute("categories", List.of("전체", "예매", "전시", "취소/환불", "동행", "회원"));
        
        return "support/faq";
    }

    // [8. 환불 안내 페이지]
    @GetMapping("/refund")
    public String refundInfo() {
        return "support/refund"; 
    }
}