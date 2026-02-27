package com.culti.content.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.content.entity.Content;
import com.culti.content.entity.Review;
import com.culti.content.repository.ReviewRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

private final ReviewRepository reviewRepository;
private final ContentService contentService;
private final UserRepository userRepository;
	
	public Page<Review> getReviewList(Long contentId, int page, String sortType) {
		
		List<Sort.Order> sorts = new ArrayList<>();
		
		if ("high".equals(sortType)) {
            sorts.add(Sort.Order.desc("rating")); // 별점 높은 순
        } else if ("low".equals(sortType)) {
            sorts.add(Sort.Order.asc("rating"));  // 별점 낮은 순
        } else {
            sorts.add(Sort.Order.desc("createdAt")); // 기본: 최신순
        }
		
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		
		return reviewRepository.findByContentId(contentId, pageable);
	}
	
	// 파일 저장 + 리뷰 DB 저장 메서드
    @Transactional
    public void saveReview(Long contentId, int rating, String text, boolean isSpoiler, List<MultipartFile> files, String username) {
        
        Content content = contentService.getContent(contentId);
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다.")); 

        // 저장할 C드라이브 폴더 경로 설정
        String uploadDir = "C:\\culti_upload\\review\\";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // 실제 파일 저장 & 랜덤 파일명(UUID) 생성 로직
        List<String> savedFileNames = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String savedFilename = UUID.randomUUID().toString() + extension;
                    
                    try {
                        file.transferTo(new File(uploadDir + savedFilename));
                        savedFileNames.add(savedFilename);
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
                    }
                }
            }
        }
        
        // 에러의 원흉이었던 중복 코드 삭제하고 딱 한 번만 선언!
        String photoUrls = String.join(",", savedFileNames);

        // 리뷰 엔티티 만들어서 데이터 세팅
        Review review = new Review();
        review.setContent(content);
        review.setUser(user);
        review.setRating(rating);
        review.setText(text);
        review.setSpoiler(isSpoiler);
        review.setPhotoUrls(photoUrls);

        reviewRepository.save(review);
    }
	
}
