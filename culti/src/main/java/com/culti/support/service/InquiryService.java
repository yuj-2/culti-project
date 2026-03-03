package com.culti.support.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.culti.support.entity.Inquiry;
import com.culti.support.repository.InquiryRepository;

import lombok.RequiredArgsConstructor;

import com.culti.support.entity.InquiryStatus; // 이 줄을 꼭 추가해야 에러가 사라집니다!



@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;

    @Transactional
    public void saveInquiry(Inquiry inquiry) { // 매개변수가 Inquiry 객체 하나여야 합니다!
        inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getMyInquiries(Long userId) {
        return inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Inquiry getInquiryDetail(Long inquiryId) {
        return inquiryRepository.findById(inquiryId).orElse(null);
    }
    
    // ==========================================
    // [관리자 기능 추가] 
    // ==========================================

    // [추가] 관리자용: 모든 사용자의 문의 목록을 최신순으로 조회
    public List<Inquiry> getAllInquiries() {
        // ID 역순(최신순)으로 전체 데이터를 가져옵니다.
        return inquiryRepository.findAll(Sort.by(Sort.Direction.DESC, "inquiryId"));
    }

    // [수정] 컨트롤러의 'inquiryService.saveAnswer(id, answer)'와 이름을 똑같이 맞춥니다.
    @Transactional
    public void saveAnswer(Long inquiryId, String answer) {
        // 1. 해당 문의글을 찾습니다.
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문의글이 존재하지 않습니다. ID: " + inquiryId));
        
        // 2. 답변 내용을 세팅합니다.
        inquiry.setInquiryAnswer(answer);
        
        // 3. 상태를 '답변완료(ANSWERED)'로 변경합니다.
        inquiry.setInquiryStatus(InquiryStatus.ANSWERED); 
        
        // Transactional 덕분에 save를 따로 안 써도 자동으로 DB에 반영됩니다.
    }
}