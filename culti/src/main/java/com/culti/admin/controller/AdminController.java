package com.culti.admin.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.culti.admin.dto.ContentFormDTO;
import com.culti.admin.dto.PerformancePriceDTO;
import com.culti.admin.dto.SinglePriceDTO;
import com.culti.admin.service.AdminService;
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
	
	@GetMapping("/main")
	public void main() {
		
	}
	
	// 콘텐츠 목록 등록 경로
	@GetMapping("/content/new")
    public String contentForm(Model model) {
		List<Place> places = adminService.getAllPlaces();
		
		model.addAttribute("places", places);
		
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
        
        model.addAttribute("contentList", contents);
        return "admin/content-list";
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
	    
	    return "admin/place-manage";
	}

	// 2. 새 장소 등록 처리하기
	@PostMapping("/place/new")
	public String registerPlace(@RequestParam("name") String name, 
	                            @RequestParam("address") String address) {
	    adminService.registerPlace(name, address);
	    return "redirect:/admin/place/manage";
	}
	
	
}
