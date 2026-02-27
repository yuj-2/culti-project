package com.culti.content.controller;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.culti.content.entity.Content;
import com.culti.content.repository.ContentRepository;
import com.culti.content.service.ContentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;
	private final ContentRepository contentRepository;

	@GetMapping("/reservation")
    public String reservationPage() {
        return "reservation/reservation"; 
    }
	
	/*
	@GetMapping("/content/api/list")
    @ResponseBody
    public List<Content> getContentsApi() {
        return this.contentService.getList();
    }
    */
	
	@GetMapping("/content/api/list")
    @ResponseBody
    public List<Content> getList(
            @RequestParam(value = "category", defaultValue = "영화") String category,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "sort", defaultValue = "인기순") String sort
    ) {
        Sort jpaSort;
        
        if ("최신순".equals(sort)) {
            jpaSort = Sort.by(Sort.Direction.DESC, "createdAt");
        } else if ("가나다순".equals(sort)) {
            jpaSort = Sort.by(Sort.Direction.ASC, "title");
        } else {
            jpaSort = Sort.by(Sort.Direction.DESC, "bookingCount"); 
        }

        return contentRepository.findByCategoryAndTitleContainingIgnoreCase(category, keyword, jpaSort);
    }
	
	@GetMapping("/reservation/detail")
	public String detailPage() {
		return "/reservation/content-detail";
	}
	
	@GetMapping("/reservation/detail/{id}")
	public String dettailPage(Model model, @PathVariable("id") Long id) {
		Content content = this.contentService.getContent(id);
		model.addAttribute("content", content);
		return "reservation/content-detail";
	}
	
}
