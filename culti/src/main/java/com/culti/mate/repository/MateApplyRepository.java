package com.culti.mate.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.culti.auth.entity.User;
import com.culti.mate.entity.MateApply;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MateApplyStatus;

import jakarta.transaction.Transactional;

public interface MateApplyRepository extends JpaRepository<MateApply, Long>{

	
	boolean existsByPostAndApplicant(MatePost post, User applicant);
	long countByPostAndStatus(MatePost post, MateApplyStatus status);

	 // 받은 신청: 내가 작성한 글에 들어온 신청
    List<MateApply> findByPost_Writer_EmailOrderByCreatedAtDesc(String email);

    // 내가 신청한 것: 내가 신청자
    List<MateApply> findByApplicant_EmailOrderByCreatedAtDesc(String email);
    
    @Query("""
            select distinct a.post.postId
            from MateApply a
            where a.applicant.email = :email
        """)
	List<Long> findAppliedPostIdsByApplicantEmail(@Param("email") String email);
    
    @Query("""
            select ma.post.postId, ma.status
            from MateApply ma
            where ma.applicant.email = :email
        """)
	List<Object[]> findPostIdAndStatusByApplicantEmail(@Param("email") String email);
	
	@Transactional
	void deleteByPost_PostId(Long postId);
	Page<MateApply> findByApplicant_Email(String email, Pageable pageable);
	Page<MateApply> findByPost_Writer_Email(String email, Pageable pageable);
	
	
	// postId별 ACCEPTED count 집계 쿼리 추가
		  @Query("""
		    select a.post.postId, count(a)
		    from MateApply a
		    where a.post.postId in :postIds
		      and a.status = :status
		    group by a.post.postId
		  """)
		  List<Object[]> countByPostIdsAndStatusGroupByPostId(
		      @Param("postIds") List<Long> postIds,
		      @Param("status") MateApplyStatus status
		  );

		
}
