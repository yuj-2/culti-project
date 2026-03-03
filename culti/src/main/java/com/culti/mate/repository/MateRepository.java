package com.culti.mate.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.culti.mate.DTO.MatePostDTO;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MatePostCategory;
import com.culti.mate.enums.MatePostStatus;

public interface MateRepository extends JpaRepository<MatePost, Long> {

	Page<MatePost> findByCategory(MatePostCategory enumCategory, Pageable pageable);



    List<MatePost> findByWriter_EmailOrderByCreatedAtDesc(String email);
    
    long countByPostIdGreaterThan(Long postId);
    
    long countByCategoryAndPostIdGreaterThan(MatePostCategory category, Long postId);



	Page<MatePost> findByWriter_Email(String email, Pageable pageable);
	
	@Query("""
			select new com.culti.mate.DTO.MatePostDTO(
			    p.postId,
			    p.title,
			    p.category,
			    null,
			    null,
			    p.eventAt,
			    p.location,
			    p.maxPeople,
			    p.description,
			    (select count(a) from MateApply a
			        where a.post = p and a.status = com.culti.mate.enums.MateApplyStatus.ACCEPTED),
			    w.nickname,
			    p.status,
			    p.createdAt
			)
			from MatePost p join p.writer w
			where (:catEnum is null or p.category = :catEnum)
			order by p.createdAt desc
			""")
			Page<MatePostDTO> findPostListDto(@Param("catEnum") MatePostCategory catEnum, Pageable pageable);
	
	@Query("""
			select ma.post.postId, ma.status
			from MateApply ma
			where ma.applicant.email = :email
			order by ma.createdAt desc
			""")
			List<Object[]> findPostIdAndStatusByApplicantEmail(@Param("email") String email);



		Page<MatePost> findAllByOrderByCreatedAtDesc(Pageable pageable);
		Page<MatePost> findByStatusOrderByCreatedAtDesc(MatePostStatus status, Pageable pageable);

}
