package com.culti.support.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.culti.auth.dto.UserDTO;
import com.culti.auth.service.UserService; // 팀원의 유저 서비스
import com.culti.support.entity.Faq;
import com.culti.support.entity.Inquiry;
import com.culti.support.entity.Notice;
import com.culti.support.service.FaqService;
import com.culti.support.service.InquiryService;
import com.culti.support.service.NoticeService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/support")
public class InquiryController {

    private final InquiryService inquiryService;
    private final NoticeService noticeService;
    private final FaqService faqService;
    private final UserService userService; // 팀원의 서비스 주입

    /*
    // [고객센터 메인]
    @GetMapping("")
    public String supportMain(Model model, 
        @PageableDefault(size = 5, sort = "noticeId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Notice> noticePage = noticeService.getNoticeList(pageable);
        model.addAttribute("noticePage", noticePage);
        model.addAttribute("noticeList", noticePage.getContent()); 
        
        return "support/support";
    }
    */

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
    
    // =========================================================   
    // [9. 관리자 메인 페이지]
    // =========================================================
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminMain(Principal principal, Model model) {
        UserDTO userDTO = this.userService.findByEmail(principal.getName());
        model.addAttribute("user", userDTO);
        return "support/admin_main";
    }

    // [10. 관리자 문의 답변 목록]
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/inquiry")
    public String adminInquiryPage(Model model) {
        // 모든 문의를 가져오는 메서드가 서비스에 필요합니다.
        List<Inquiry> allInquiries = inquiryService.getAllInquiries(); 
        model.addAttribute("inquiries", allInquiries);
        return "support/admin_inquiry";
    }

    // [11. 답변 저장 기능]
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/inquiry/reply")
    public String replyInquiry(@RequestParam("inquiryId") Long id, @RequestParam("answer") String answer) {
        inquiryService.saveAnswer(id, answer); // 서비스에 답변 저장 메서드 구현 필요
        return "redirect:/support/admin/inquiry";
    }
    
    @GetMapping("")
    public String supportMain(Principal principal, Model model, 
        @PageableDefault(size = 5, sort = "noticeId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        if (principal != null) {
            UserDTO userDTO = this.userService.findByEmail(principal.getName());
            String role = userDTO.getRole();
            
            // DB에 ROLE_ADMIN 또는 ADMIN으로 저장되어 있을 수 있으므로 둘 다 체크
            if ("ADMIN".equals(role) || "ROLE_ADMIN".equals(role)) {
                return "redirect:/support/admin/inquiry";
            }
        }

        Page<Notice> noticePage = noticeService.getNoticeList(pageable);
        model.addAttribute("noticePage", noticePage);
        model.addAttribute("noticeList", noticePage.getContent()); 
        
        return "support/support";
    }
    
    // =========================================================
    // [ 12. 관리자 모드 - 공지사항 관리 ]
    // =========================================================
    // 1. 공지사항 관리 메인 목록 (localhost/support/admin/notice)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/notice")
    public String adminNoticePage(Model model, 
        @PageableDefault(size = 10, sort = "noticeId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Notice> noticePage = noticeService.getNoticeList(pageable);
        model.addAttribute("noticePage", noticePage);
        return "support/admin_notice"; // admin_notice.html 필요
    }

    // 2. 공지사항 작성 페이지 이동
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/notice/write")
    public String noticeWritePage() {
        return "support/admin_notice_write"; // admin_notice_write.html 필요
    }

    // 3. 공지사항 저장 로직
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/notice/save")
    public String saveNotice(Principal principal, 
                             @RequestParam("title") String title, 
                             @RequestParam("content") String content) {
        
        // 1. 현재 로그인한 관리자 정보 가져오기
        UserDTO userDTO = userService.findByEmail(principal.getName());
        
        // 2. 빌더 사용 (엔티티 생성자에 정의된 필드만 사용)
        Notice notice = Notice.builder()
                .userId(userDTO.getUserId()) // 필수 값!
                .noticeTitle(title)
                .noticeContent(content)
                // .viewCount(0) <- 이 줄을 삭제하세요. 엔티티 생성자에서 이미 0으로 처리함
                .build();
                
        noticeService.saveNotice(notice); 
        return "redirect:/support/admin/notice";
    }

    // 4. 공지사항 삭제 로직
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/notice/delete/{id}")
    public String deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return "redirect:/support/admin/notice";
    }

    // =========================================================
    // [ 관리자 모드 - FAQ 관리 ]
    // =========================================================
    // 1. FAQ 관리 메인 목록 (localhost/support/admin/faq)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/faq")
    public String adminFaqPage(Model model, 
        @PageableDefault(size = 10, sort = "faqId", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<Faq> faqPage = faqService.getFaqList(pageable);
        model.addAttribute("faqPage", faqPage);
        return "support/admin_faq"; // admin_faq.html 필요
    }

    // 2. FAQ 작성 페이지 이동
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/faq/write")
    public String faqWritePage(Model model) {
        model.addAttribute("categories", List.of("예매", "전시", "취소/환불", "동행", "회원"));
        return "support/admin_faq_write"; // admin_faq_write.html 필요
    }

    // 3. FAQ 저장 로직
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/faq/save")
    public String saveFaq(@RequestParam("category") String category, 
                           @RequestParam("content") String content, // 질문 내용
                           @RequestParam("answer") String answer) { // 답변 내용
        
        // 엔티티의 @Builder 생성자 파라미터 이름(faqCategory, faqContent, faqAnswer)과 일치시켜야 합니다.
        Faq faq = Faq.builder()
                .faqCategory(category) 
                .faqContent(content)   // 질문(Content)
                .faqAnswer(answer)     // 답변(Answer)
                .build();
                
        faqService.saveFaq(faq);
        return "redirect:/support/admin/faq";
    }

    // 4. FAQ 삭제 로직
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/faq/delete/{id}")
    public String deleteFaq(@PathVariable("id") Long id) {
        faqService.deleteFaq(id);
        return "redirect:/support/admin/faq";
    }
    
}