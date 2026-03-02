package com.culti.mate.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.mate.DTO.MateApplyMypageDTO;
import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.entity.MateApply;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MateApplyStatus;
import com.culti.mate.enums.MatePostCategory;
import com.culti.mate.enums.MatePostStatus;
import com.culti.mate.repository.MateApplyRepository;
import com.culti.mate.repository.MateRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MateServiceImpl implements MateService {
	
	private final MateRepository mateRepository;
	
	private final UserRepository userRepository;
	
	private final MateApplyRepository mateApplyRepository;

	public Page<MatePost> getList(int page, int size){
		//                                  0(1번페이지)
		Pageable pageable = PageRequest.of(page-1, size, Sort.by("postId").descending());
		return this.mateRepository.findAll(pageable);
	}
	
	public void addPost(MatePostDTO dto, String email) {
	    User writer = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + email));

	    LocalDateTime eventAt = LocalDateTime.of(
	            LocalDate.parse(dto.getDate()),
	            LocalTime.parse(dto.getTime())
	    );

	    dto.setEventAt(eventAt); // ★ 이 한 줄이 핵심 (DTO가 @Data라 setter 있음)

	    MatePost entity = this.dtoToEntity(dto, writer, eventAt);
	    mateRepository.save(entity);
	}

	public Page<MatePost> getListByCategory(String category, int page, int size) {
	    Pageable pageable = PageRequest.of(page - 1, size, Sort.by("postId").descending());

	    if (category == null || category.equalsIgnoreCase("all")) {
	        return mateRepository.findAll(pageable);
	    }

	    // movie/concert/exhibition -> enum으로 변환
	    MatePostCategory enumCategory = MatePostCategory.valueOf(category.toUpperCase());
	    return mateRepository.findByCategory(enumCategory, pageable);
	}

	@Override
	public void delete(Long postId, String email) {
		MatePost post = mateRepository.findById(postId)
	            .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

	    // 작성자 본인인지 체크
	    if (!post.getWriter().getEmail().equals(email)) {
	        throw new SecurityException("삭제 권한이 없습니다.");
	        
	    }
	    mateRepository.deleteById(postId);
	    
	}

	@Override
	@Transactional
	public void apply(Long postId, String email, String message) {
//		글 작성자가 신청하면 예외
//		이미 신청했으면 예외
//		모집 마감이면 예외
		 // 1) 게시글 조회
	    MatePost post = mateRepository.findById(postId)
	            .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

	    // 2) 신청자 조회
	    User applicant = userRepository.findByEmail(email)
	            .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + email));

	    // 3) 작성자가 자기 글 신청 금지
	    if (post.getWriter().getEmail().equals(email)) {
	        throw new IllegalStateException("작성자는 본인 글에 신청할 수 없습니다.");
	    }

	    // 4) 모집 마감 체크
	    if (post.getStatus() == MatePostStatus.CLOSED) {
	        throw new IllegalStateException("모집이 마감된 게시글입니다.");
	    }

	    // 5) 이미 신청했는지 체크 (중복 방지)
	    if (mateApplyRepository.existsByPostAndApplicant(post, applicant)) {
	        throw new IllegalStateException("이미 신청한 게시글입니다.");
	    }

	    // 6) 인원 체크 (승인된 인원 기준)
	    long approvedCount = mateApplyRepository.countByPostAndStatus(post, MateApplyStatus.ACCEPTED);
	    if (approvedCount >= post.getMaxPeople()) {
	        throw new IllegalStateException("모집 인원이 모두 찼습니다.");
	    }

	    // 7) 신청 저장	    
	    MateApply apply = MateApply.builder()
	            .post(post)
	            .applicant(applicant)
	            .message(message)
	            .status(MateApplyStatus.PENDING)
	            .decidedAt(null)
	            .build();

	    mateApplyRepository.save(apply);
		
		
	}

	@Override
	public void addComment(Long postId, String email, String content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateComment(Long commentId, String email, String content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteComment(Long commentId, String email) {
		// TODO Auto-generated method stub
		
	}

	// 신청 수락
	@Transactional
	public void accept(Long applyId, String email) {

	    MateApply apply = mateApplyRepository.findById(applyId)
	        .orElseThrow(() -> new IllegalArgumentException("신청 없음"));

	    // 게시글 작성자만 가능
	    if (!apply.getPost().getWriter().getEmail().equals(email)) {
	        throw new SecurityException("권한 없음");
	    }
	    
	    if (apply.getStatus() != MateApplyStatus.PENDING) {
	        throw new IllegalStateException("이미 처리된 신청입니다.");
	    }
	    
	    long approvedCount =
	    	    mateApplyRepository.countByPostAndStatus(
	    	        apply.getPost(), MateApplyStatus.ACCEPTED);

    	if (approvedCount >= apply.getPost().getMaxPeople()) {
    	    throw new IllegalStateException("모집 인원이 모두 찼습니다.");
    	}
    	
    	apply.accept();

    	if (approvedCount + 1 >= apply.getPost().getMaxPeople()) {
    	    apply.getPost().close();
    	}
	    mateApplyRepository.save(apply);
	}
	
	// 거절
	@Transactional
	public void reject(Long applyId, String email) {
	    MateApply apply = mateApplyRepository.findById(applyId)
	            .orElseThrow(() -> new IllegalArgumentException("신청 없음: " + applyId));

	    // 작성자만 가능
	    if (!apply.getPost().getWriter().getEmail().equals(email)) {
	        throw new SecurityException("권한 없음");
	    }

	    if (apply.getStatus() != MateApplyStatus.PENDING) {
	        throw new IllegalStateException("이미 처리된 신청입니다.");
	    }

	    apply.reject();
	    mateApplyRepository.save(apply);
	}

	//취소
	@Transactional
	public void cancel(Long applyId, String email) {
	    MateApply apply = mateApplyRepository.findById(applyId)
	            .orElseThrow(() -> new IllegalArgumentException("신청 없음: " + applyId));

	    // 신청자만 가능
	    if (!apply.getApplicant().getEmail().equals(email)) {
	        throw new SecurityException("권한 없음");
	    }

	    if (apply.getStatus() != MateApplyStatus.PENDING) {
	        throw new IllegalStateException("대기중(PENDING)인 신청만 취소할 수 있습니다.");
	    }

	    apply.cancel();

	    // (선택) 혹시 게시글이 CLOSED였고, 승인 인원 미달이 되면 OPEN으로 되돌릴지?
	    // 지금은 PENDING만 취소 가능이라 approvedCount에 영향 없음 -> 생략 가능

	    mateApplyRepository.save(apply);
	}
	
//	===============
	@Override
	public List<MateApplyMypageDTO> getReceivedApps(String email) {
	    return mateApplyRepository.findByPost_Writer_EmailOrderByCreatedAtDesc(email)
	            .stream()
	            .map(this::toMyPageDto)
	            .toList();
	}

	@Override
	public List<MateApplyMypageDTO> getSentApps(String email) {
	    return mateApplyRepository.findByApplicant_EmailOrderByCreatedAtDesc(email)
	            .stream()
	            .map(this::toMyPageDto)
	            .toList();
	}

	private MateApplyMypageDTO toMyPageDto(MateApply a) {
	    MatePost p = a.getPost();

	    return MateApplyMypageDTO.builder()
	            .id(a.getApplyId())
	            .postTitle(p.getTitle())
	            .applicantNickname(a.getApplicant().getNickname())
	            .writerNickname(p.getWriter().getNickname())
	            .createdAt(a.getCreatedAt())
	            .eventAtText(p.getEventAt() != null ? p.getEventAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "")
	            .location(p.getLocation())
	            .status(a.getStatus().name()) // "PENDING"
	            .statusLabel(statusLabel(a.getStatus()))
	            .build();
	}
	
	private String statusLabel(MateApplyStatus s) {
	    return switch (s) {
	        case PENDING -> "대기중";
	        case ACCEPTED -> "수락됨";
	        case REJECTED -> "거절됨";
	        case CANCELED -> "취소됨";
	    };
	}
	
	
	@Override
	public List<Long> getAppliedPostIds(String applicantEmail) {
	    return mateApplyRepository.findAppliedPostIdsByApplicantEmail(applicantEmail);
	}

	
}
