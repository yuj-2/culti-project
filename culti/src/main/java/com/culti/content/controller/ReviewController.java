package com.culti.content.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.culti.content.entity.Content;
import com.culti.content.entity.Review;
import com.culti.content.service.ContentService;
import com.culti.content.service.ReviewService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ReviewController {

	private final ContentService contentService;
	private final ReviewService reviewService;
	
	@GetMapping("/reservation/detail/{id}/reviews")
	public String reviewListPage(
			@PathVariable("id") Long id,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "sort", defaultValue = "latest") String sort,
			@RequestParam(value = "photoOnly", defaultValue = "false") boolean photoOnly,
			Model model
			) {
		Content content = contentService.getContent(id);
		Page<Review> paging = reviewService.getReviewList(id, page, sort, photoOnly);
		
		int totalReviews = content.getReviews().size();
        double averageRating = 0.0;
        int[] ratingCounts = new int[6];
        int[] ratingPercentages = new int[6];
        
        if (totalReviews > 0) {
            double sum = 0;
            for (Review r : content.getReviews()) {
                sum += r.getRating();
                if(r.getRating() >= 1 && r.getRating() <= 5) {
                    ratingCounts[r.getRating()]++;
                }
            }
            // 평균 별점 (소수점 첫째 자리까지 반올림)
            averageRating = Math.round((sum / totalReviews) * 10.0) / 10.0; 

            // 각 별점별 퍼센트 계산
            for (int i = 1; i <= 5; i++) {
                ratingPercentages[i] = (int) Math.round(((double) ratingCounts[i] / totalReviews) * 100);
            }
        }
        
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("ratingCounts", ratingCounts);
        model.addAttribute("ratingPercentages", ratingPercentages);
        
		model.addAttribute("content", content);
        model.addAttribute("paging", paging);
        model.addAttribute("currentSort", sort);
        
        model.addAttribute("photoOnly", photoOnly);
        
        return "review/review-detail";
        
	}
	
	// 리뷰 작성 페이지 띄워주기
	@GetMapping("/reservation/detail/{id}/review/write")
    public String reviewWritePage(@PathVariable("id") Long id, Model model) {
        Content content = contentService.getContent(id);
        model.addAttribute("content", content);
        
        return "review/review-write";
    }
	
	// 작성할 리뷰 데이터 받아서 저장하기
	@PostMapping("/reservation/detail/{id}/review/write")
    public String submitReview(
            @PathVariable("id") Long id,
            @RequestParam("rating") int rating,
            @RequestParam("text") String text,
            @RequestParam(value = "isSpoiler", defaultValue = "false") boolean isSpoiler,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            Principal principal
    ) {
		
		String username = (principal != null) ? principal.getName() : "test@naver.com";
		
        reviewService.saveReview(id, rating, text, isSpoiler, files, username);

		return "redirect:/reservation/detail/" + id + "/reviews";
	}
	
}
