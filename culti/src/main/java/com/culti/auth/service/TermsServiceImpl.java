package com.culti.auth.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.auth.dto.TermsDTO;
import com.culti.auth.entity.Terms;
import com.culti.auth.entity.User;
import com.culti.auth.entity.UserTerms;
import com.culti.auth.repository.TermsRepository;
import com.culti.auth.repository.UserTermsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService{
	private final TermsRepository termsRepository;
	private final UserTermsRepository userTermsRepository;
	@Override
	public List<TermsDTO> getActiveTerms() {
		return termsRepository.findAllByIsActive("Y").stream()
                .map(t -> new TermsDTO(t.getId(), t.getTitle(), t.getContent(), t.getIsRequired(), t.getVersion(),t.getCreatedAt()))
                .collect(Collectors.toList());
	}
	@Transactional
	@Override
	public void saveUserAgreements(User user, List<Long> agreedTermIds) {
		for (Long termId : agreedTermIds) {
	        Terms terms = termsRepository.findById(termId)
	                .orElseThrow(() -> new IllegalArgumentException("약관이 존재하지 않습니다. ID: " + termId));

	        UserTerms userTerms = UserTerms.builder()
	                .user(user)
	                .terms(terms)
	                .build();

	        userTermsRepository.save(userTerms);
	    }
		
	}
	
}
