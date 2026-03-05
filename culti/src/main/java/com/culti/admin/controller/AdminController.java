package com.culti.admin.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.culti.admin.dto.ContentFormDTO;
import com.culti.admin.dto.PerformancePriceDTO;
import com.culti.admin.dto.SinglePriceDTO;
import com.culti.admin.service.AdminService;
import com.culti.auth.dto.UserDTO;
import com.culti.auth.entity.LoginLog;
import com.culti.auth.entity.User;
import com.culti.booking.entity.Place;
import com.culti.content.entity.Content;
import com.culti.content.entity.ContentPrice;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Log4j2
public class AdminController {

	private final AdminService adminService;
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/main")
	public void main(Model model) {
		model.addAttribute("titleName", "통계 대시보드");
	}
	
	// 콘텐츠 목록 등록 경로
	@GetMapping("/content/new")
    public String contentForm(Model model) {
		List<Place> places = adminService.getAllPlaces();
		
		model.addAttribute("places", places);
		model.addAttribute("titleName", "콘텐츠 등록");
        return "admin/content-form";
    }
	
	@PostMapping("/content/new")
	public String registerContent(@ModelAttribute ContentFormDTO formDTO) {
	    try {
	        adminService.registerContent(formDTO);
	        
	    } catch (Exception e) {
	        log.error("콘텐츠 등록 중 에러 발생!", e);
	        
	        return "redirect:/admin/content/new?error"; 
	    }
	    
	    return "redirect:/admin/content/list";
	}
	
	@GetMapping("/content/list")
    public String contentList(@RequestParam(value = "category", required = false) String category, org.springframework.ui.Model model) {
        // 서비스에서 카테고리별 또는 전체 리스트를 가져옴
        java.util.List<com.culti.content.entity.Content> contents;
        
        if (category == null || category.equals("ALL")) {
            contents = adminService.getAllContents();
        } else {
            contents = adminService.getContentsByCategory(category);
        }
        model.addAttribute("titleName", "영화/전시 관리");
        model.addAttribute("contentList", contents);
        return "admin/content-list";
    }
	
	@PostMapping("/content/edit") 
	public String editContent(@ModelAttribute com.culti.content.dto.ContentDTO contentDTO,
	                          @RequestParam(value = "posterFile", required = false) MultipartFile posterFile) {
	                          
	    adminService.updateContent(contentDTO.getId(), contentDTO, posterFile);
	    
	    return "redirect:/admin/content/list"; 
	}
	
	@GetMapping("/content/edit")
	public String contentEditForm(@RequestParam("id") Long id, Model model) {
	    // 1. 기존에 등록된 콘텐츠 정보 싹 다 가져오기
	    Content content = adminService.getContentById(id);
	    
	    // 2. 장소 수정할 때 써먹을 전체 장소 리스트 가져오기
	    List<Place> places = adminService.getAllPlaces();
	    
	    // 3. 모델에 담아서 수정 화면으로 쏘기
	    model.addAttribute("content", content);
	    model.addAttribute("places", places);
	    
	    return "admin/content-edit"; 
	}
	
	// 1. 가격 등록 폼 화면 보여주기
	@GetMapping("/price/performance")
	public String performancePriceForm(@RequestParam("id") Long contentId, Model model) {
	    // 1. 해당 콘텐츠 정보와 기존 가격 리스트를 DB에서 꺼내옴
	    Content content = adminService.getContentById(contentId); // Service에 이 메서드가 있어야 합니다!
	    
	    // 2. 화면에 뿌려줄 바구니(DTO) 준비
	    PerformancePriceDTO priceDTO = new PerformancePriceDTO();
	    priceDTO.setContentId(contentId);
	    
	    // 3. 기존 가격이 있다면 바구니에 담기
	    if (content.getContentPrices() != null) {
	        for (ContentPrice cp : content.getContentPrices()) {
	            switch (cp.getGrade()) {
	                case "VIP": priceDTO.setVipPrice(cp.getPrice()); break;
	                case "R": priceDTO.setRPrice(cp.getPrice()); break;
	                case "S": priceDTO.setSPrice(cp.getPrice()); break;
	                case "A": priceDTO.setAPrice(cp.getPrice()); break;
	            }
	        }
	    }
	    
	    // 4. 모델에 담아서 HTML로 넘기기
	    model.addAttribute("priceDTO", priceDTO);
	    return "admin/price-performance-form";
	}

	// 2. 입력받은 가격 폼 제출받아서 처리하기
	@PostMapping("/price/performance")
	public String registerPerformancePrice(PerformancePriceDTO dto) {
	    adminService.registerPerformancePrice(dto);
	    return "redirect:/admin/content/list";
	}
	
	// 전시, 팝업 가격 등록
	@GetMapping("/price/single")
	public String singlePriceForm(@RequestParam("id") Long contentId, Model model) {
	    Content content = adminService.getContentById(contentId);
	    
	    SinglePriceDTO priceDTO = new SinglePriceDTO();
	    priceDTO.setContentId(contentId);
	    
	    if (content.getContentPrices() != null) {
	        for (ContentPrice cp : content.getContentPrices()) {
	            if ("일반".equals(cp.getGrade())) {
	                priceDTO.setPrice(cp.getPrice());
	                break;
	            }
	        }
	    }
	    
	    model.addAttribute("priceDTO", priceDTO);
	    return "admin/price-single-form"; 
	}

	@PostMapping("/price/single")
	public String registerSinglePrice(SinglePriceDTO dto) {
	    adminService.registerSinglePrice(dto);
	    return "redirect:/admin/content/list";
	}
	
	@Value("${kakao.api.js-key}") 
	private String kakaoJsKey;
	
	// 1. 장소 관리 페이지 화면 띄우기
	@GetMapping("/place/manage")
	public String placeManagePage(Model model) {
	    model.addAttribute("placeList", adminService.getAllPlaces());
	    model.addAttribute("kakaoJsKey", kakaoJsKey);
	    model.addAttribute("titleName", "장소 관리");
	    return "admin/place-manage";
	}

	// 2. 새 장소 등록 처리하기
	@PostMapping("/place/new")
	public String registerPlace(@RequestParam("name") String name, 
	                            @RequestParam("address") String address) {
	    adminService.registerPlace(name, address);
	    return "redirect:/admin/place/manage";
	}
	
	
	@GetMapping("/auth/user")
	public String userList(
	        Model model,
	        @RequestParam(name = "keyword", required = false) String keyword) {

	    // 검색어가 null 또는 빈 문자열이면 null 처리
	    if (keyword != null && keyword.isBlank()) {
	        keyword = null;
	    }

	    List<UserDTO> userList = this.adminService.getUserDTOs(keyword);
	    model.addAttribute("userList", userList);
	    model.addAttribute("keyword", keyword); // 뷰에서 검색어 유지용
	    model.addAttribute("titleName", "회원 관리");
	    return "admin/auth-user";
	}
	
	@PostMapping("/user/role/{id}")
	public String changeRole(@PathVariable("id") Long id) {
		System.out.println(id);
	    this.adminService.toggleUserRole(id);
	    return "redirect:/admin/auth/user";
	}
	
	@GetMapping("/auth/loginLog")
	public String loginLogPage(Model model) {
	    List<LoginLog> loginLogs = this.adminService.findAllLog();
	    model.addAttribute("loginLogs", loginLogs);
	    return "admin/auth-logInLog";
	}
	
}
