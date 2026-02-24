package com.culti.content;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
	
	@GetMapping("/content/api/list")
    @ResponseBody
    public List<Content> getContentsApi() {
        return this.contentService.getList();
    }
	
	@GetMapping("/reservation/detail")
	public String detailPage() {
		return "/reservation/content-detail";
	}
	
}
