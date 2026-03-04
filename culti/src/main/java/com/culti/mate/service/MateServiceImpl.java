package com.culti.mate.service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.culti.auth.entity.User;
import com.culti.auth.repository.UserRepository;
import com.culti.mate.DTO.MateApplyMypageDTO;
import com.culti.mate.DTO.MateCommentDTO;
import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.DTO.MyPostMypageDTO;
import com.culti.mate.DTO.PageResultDTO;
import com.culti.mate.entity.MateApply;
import com.culti.mate.entity.MateComment;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MateApplyStatus;
import com.culti.mate.enums.MatePostCategory;
import com.culti.mate.enums.MatePostStatus;
import com.culti.mate.repository.MateApplyRepository;
import com.culti.mate.repository.MateCommentRepository;
import com.culti.mate.repository.MateRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MateServiceImpl implements MateService {
	
	private final MateRepository mateRepository;
	
	private final UserRepository userRepository;
	
	private final MateApplyRepository mateApplyRepository;
	
	private final MateCommentRepository mateCommentRepository;  
	

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

		    if (!post.getWriter().getEmail().equals(email)) {
		        throw new SecurityException("삭제 권한이 없습니다.");
		    }

		    // 자식 먼저 삭제
		    mateApplyRepository.deleteByPost_PostId(postId);

		    // 부모 삭제
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
	            .orElseThrow(() -> new IllegalArgumentException("신청이 존재하지 않습니다."));

	    MatePost post = apply.getPost();

	    // 글쓴이만 수락 가능
	    if (!post.getWriter().getEmail().equals(email)) {
	        throw new IllegalStateException("권한이 없습니다.");
	    }

	    // PENDING만 처리 가능
	    if (apply.getStatus() != MateApplyStatus.PENDING) {
	        throw new IllegalStateException("이미 처리된 신청입니다.");
	    }

	    // 현재 확정 인원(글쓴이 제외 = 신청 ACCEPTED 수)
	    long acceptedCount = mateApplyRepository.countByPostAndStatus(post, MateApplyStatus.ACCEPTED);

	    // 정원 초과 방지
	    if (acceptedCount >= post.getMaxPeople()) {
	        throw new IllegalStateException("이미 모집이 마감되었습니다.");
	    }

	    // 수락 처리 (여기서 status=ACCEPTED, decidedAt=now 처리됨)
	    apply.accept();

	    // 이번 수락으로 정원 꽉 차면 마감
	    if (acceptedCount + 1 >= post.getMaxPeople()) {
	        post.close(); // setStatus 말고 close()
	    }
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
	            .decidedAt(a.getDecidedAt())
	            .message(a.getMessage())
	            .category(p.getCategory().name())
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

	@Override
	public Map<Long, MateApplyStatus> getAppliedStatusMap(String email) {
		List<Object[]> rows = mateApplyRepository.findPostIdAndStatusByApplicantEmail(email);

	    Map<Long, MateApplyStatus> map = new HashMap<>();
	    for (Object[] row : rows) {
	        Long postId = (Long) row[0];
	        MateApplyStatus status = (MateApplyStatus) row[1];
	        map.put(postId, status);
	    }
	    return map;
	}

	@Override
	public List<MyPostMypageDTO> getMyPosts(String email) {
	    return mateRepository.findByWriter_EmailOrderByCreatedAtDesc(email)
	            .stream()
	            .map(this::toMyPostDto)
	            .toList();
	}
	
	private MyPostMypageDTO toMyPostDto(MatePost p) {
	    return MyPostMypageDTO.builder()
	            .postId(p.getPostId()) // 네 엔티티 PK명이 postId면 이거
	            .title(p.getTitle())
	            .category(p.getCategory() != null ? p.getCategory().name() : "")
	            .categoryLabel(categoryLabel(p.getCategory()))
	            .createdAt(p.getCreatedAt())
	            .eventAtText(p.getEventAt() != null
	                    ? p.getEventAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
	                    : "")
	            .location(p.getLocation())
	            .status(p.getStatus() != null ? p.getStatus().name() : "")
	            .statusLabel(postStatusLabel(p.getStatus()))
	            
	            .build();
	}
	
	private String categoryLabel(MatePostCategory c) {
	    if (c == null) return "";
	    return switch (c) {
	        case MOVIE -> "영화";
	        case CONCERT -> "공연";
	        case EXHIBITION -> "전시";
	    };
	}

	private String postStatusLabel(MatePostStatus s) {
	    if (s == null) return "";
	    return switch (s) {
	        case OPEN -> "모집중";
	        case CLOSED -> "마감";
	    };
	}
	
	@Override
	public int calcPageForPost(Long postId, int size, MatePostCategory category) {

	    long beforeCount;

	    if (category == null) {
	        // 전체(all) 기준
	        beforeCount = mateRepository.countByPostIdGreaterThan(postId);
	    } else {
	        // 특정 카테고리 기준
	        beforeCount = mateRepository.countByCategoryAndPostIdGreaterThan(category, postId);
	    }

	    long position = beforeCount + 1;

	    return (int)((position - 1) / size) + 1;
	}
	
	@Override
	public PageResultDTO<MyPostMypageDTO, MatePost>
	getMyPostsPage(String email, int page, int size) {

	    Pageable pageable =
	            PageRequest.of(page - 1, size, Sort.by("postId").descending());

	    Page<MatePost> result =
	            mateRepository.findByWriter_Email(email, pageable);

	    return new PageResultDTO<>(
	            result,
	            this::toMyPostDto   // MatePost → MyPostMypageDTO 변환
	    );
	}
	
	public Page<MatePost> getMyPosts(String email, int page, int size) {
	    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
	    return mateRepository.findByWriter_Email(email, pageable);
	}

	@Override
	public Page<MateApplyMypageDTO> getMyApplied(String email, int page, int size) {
	    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
	    return mateApplyRepository.findByApplicant_Email(email, pageable)
	            .map(this::toMyPageDto);   // ✅ message 포함 DTO
	}

	@Override
	public Page<MateApplyMypageDTO> getReceivedApplies(String email, int page, int size) {
	    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
	    return mateApplyRepository.findByPost_Writer_Email(email, pageable)
	            .map(this::toMyPageDto);
	}

	@Override
	public Page<MyPostMypageDTO> getMyPostsDto(String email, int page, int size) {
	    Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
	    return mateRepository.findByWriter_Email(email, pageable)
	            .map(this::toMyPostDto); // eventAtText 포함
	}

	public Map<Long, Long> getAcceptedCountMap(Page<MatePost> paging) {

		  List<Long> postIds = paging.getContent().stream()
		      .map(MatePost::getPostId)
		      .toList();

		  Map<Long, Long> map = new HashMap<>();
		  if (postIds.isEmpty()) return map;

		  List<Object[]> rows = mateApplyRepository
		      .countByPostIdsAndStatusGroupByPostId(postIds, MateApplyStatus.ACCEPTED);

		  for (Object[] r : rows) {
		    Long postId = (Long) r[0];
		    Long cnt = (Long) r[1];
		    map.put(postId, cnt);
		  }

		  // 없는 글은 0으로 처리하고 싶으면(선택)
		  for (Long id : postIds) map.putIfAbsent(id, 0L);

		  return map;
		}

	 public List<MatePost> getLatestPosts(int limit) {
		    Pageable pageable = PageRequest.of(0, limit);
		    return mateRepository
		            .findByStatusOrderByCreatedAtDesc(MatePostStatus.OPEN, pageable)
		            .getContent();
		}

	 public Map<Long, Long> getAcceptedCountMap(List<MatePost> posts) {
	     if (posts == null || posts.isEmpty()) {
	         return java.util.Collections.emptyMap();
	     }

	     // 여기서 DB count 조회해서 map 만들기
	     // return resultMap;

	     return java.util.Collections.emptyMap(); // 임시라도 null 금지
	 }

	 
	 @Override
	 @Transactional
	 public MateCommentDTO addComment(Long postId, String email, String content) {

	     MatePost post = mateRepository.findById(postId)
	             .orElseThrow(() -> new IllegalArgumentException("게시글 없음: " + postId));

	     User writer = userRepository.findByEmail(email)
	             .orElseThrow(() -> new IllegalArgumentException("사용자 없음: " + email));

	     // 댓글 권한: 게시글 작성자 OR 신청 ACCEPTED인 사람
	     boolean isOwner = post.getWriter().getEmail().equals(email);
	     boolean isAccepted = mateApplyRepository.existsByPostAndApplicant_EmailAndStatus(
	             post, email, MateApplyStatus.ACCEPTED
	     );

	     if (!isOwner && !isAccepted) {
	         throw new SecurityException("댓글 권한이 없습니다.");
	     }

	     MateComment saved = mateCommentRepository.save(
	             MateComment.builder()
	                     .post(post)
	                     .writer(writer)
	                     .body(content)
	                     .build()
	     );

	     return MateCommentDTO.builder()
	             .commentId(saved.getCommentId())
	             .postId(postId)
	             .content(saved.getBody())
	             .writerNickname(saved.getWriter().getNickname())
	             .writerEmail(saved.getWriter().getEmail())
	             .createdAt(saved.getCreatedAt())
	             .build();
	 }
	 
	 @Override
	 public List<MateCommentDTO> getComments(Long postId) {

	     MatePost post = mateRepository.findById(postId)
	             .orElseThrow(() -> new RuntimeException("게시글 없음"));

	     List<MateComment> comments =
	             mateCommentRepository.findByPostOrderByCreatedAtAsc(post);

	     return comments.stream()
	             .map((MateComment c) -> MateCommentDTO.builder()
	                     .commentId(c.getCommentId())   // ✅ 엔티티 실제 getter 확인
	                     .postId(postId)
	                     .content(c.getBody())       //  엔티티 실제 getter 확인
	                     .writerNickname(c.getWriter().getNickname())
	                     .writerEmail(c.getWriter().getEmail())
	                     .createdAt(c.getCreatedAt())
	                     .build()
	             )
	             .toList(); // JDK17 OK
	 }


	
}
