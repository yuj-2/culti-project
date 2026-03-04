package com.culti.auth.service;

import java.util.List;

import com.culti.auth.dto.TermsDTO;
import com.culti.auth.entity.User;

public interface TermsService {
	
	//현재 활성화된 약관들을 List 형태로 반환
	public List<TermsDTO> getActiveTerms();
	
	//사용자가 동의한 약관들을 테이블에 저장
	public void saveUserAgreements(User user, List<Long> agreedTermIds);
}
