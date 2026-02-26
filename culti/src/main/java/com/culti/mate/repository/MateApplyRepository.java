package com.culti.mate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.auth.entity.User;
import com.culti.mate.entity.MateApply;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MateApplyStatus;

public interface MateApplyRepository extends JpaRepository<MateApply, Long>{

	
	boolean existsByPostAndApplicant(MatePost post, User applicant);
	long countByPostAndStatus(MatePost post, MateApplyStatus status);

	 // ✅ 받은 신청: 내가 작성한 글에 들어온 신청
    List<MateApply> findByPost_Writer_EmailOrderByCreatedAtDesc(String email);

    // ✅ 내가 신청한 것: 내가 신청자
    List<MateApply> findByApplicant_EmailOrderByCreatedAtDesc(String email);
	
}
