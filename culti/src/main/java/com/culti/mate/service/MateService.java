package com.culti.mate.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;

import com.culti.auth.entity.User;
import com.culti.mate.DTO.MateApplyDTO;
import com.culti.mate.DTO.MateApplyMypageDTO;
import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.entity.MateApply;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MateApplyStatus;
import com.culti.mate.enums.MatePostStatus;


public interface MateService {
	

	
//	public List<MatePost> getList(){
//		return this.mateRepository.findAll();
//	}
	
	
	// DTO -> Entity 변환 메서드
	default MatePost dtoToEntity(MatePostDTO dto, User writer, LocalDateTime eventAt) {
		MatePost entity = MatePost.builder()
				.title(dto.getTitle())
		        .category(dto.getCategory())
		        .eventAt(eventAt)
		        .location(dto.getLocation())
		        .maxPeople(dto.getMaxPeople())
		        .description(dto.getDescription())	
		        .status(MatePostStatus.OPEN)
		        .writer(writer)
				.build();		
		return entity;
	}

	// Entity -> DTO 변환 메서드
	default MatePostDTO entityToDto(MatePost entity, LocalDateTime eventAt) {

	    return MatePostDTO.builder()
	            .postId(entity.getPostId())
	            .title(entity.getTitle())
	            .category(entity.getCategory())
	            .eventAt(eventAt)
	            .location(entity.getLocation())
	            .maxPeople(entity.getMaxPeople())
	            .description(entity.getDescription())
	            .writerNickname(entity.getWriter().getNickname())
	            .build();
	}
	
	private MateApplyDTO maToDTO(MateApply ma) {

	    return MateApplyDTO.builder()
	            .id(ma.getApplyId())
	            .postTitle(ma.getPost().getTitle())
	            .location(ma.getPost().getLocation())
	            .eventAtText(
	                ma.getPost().getEventAt().format(
	                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
	                )
	            )
	            .applicantNickname(ma.getApplicant().getNickname())
	            .writerNickname(ma.getPost().getWriter().getNickname())
	            .status(ma.getStatus())
	            .statusLabel(convertStatus(ma.getStatus()))
	            .createdAt(ma.getCreatedAt())
	            .build();
	}
	
	private String convertStatus(MateApplyStatus status) {
	    return switch (status) {
	        case PENDING -> "대기중";
	        case ACCEPTED -> "수락됨";
	        case REJECTED -> "거절됨";
	        case CANCELED -> "취소됨";
	    };
	}
	


	
	void accept(Long applyId, String email);
	void reject(Long applyId, String email);
	void cancel(Long applyId, String email);
	
	public Page<MatePost> getList(int page, int size);

	public void addPost(MatePostDTO dto, String email);
	
	public Page<MatePost> getListByCategory(String category, int page, int size);

	void delete(Long postId, String email);

	public void apply(Long postId, String email, String message);

	public void addComment(Long postId, String email, String content);

	public void updateComment(Long commentId, String email, String content);

	public void deleteComment(Long commentId, String email);
	
	public List<MateApplyMypageDTO> getReceivedApps(String email);
	public List<MateApplyMypageDTO> getSentApps(String email);

}
