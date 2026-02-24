package com.culti.support.service;

import com.culti.support.entity.Inquiry;
import com.culti.support.repository.InquiryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;

    @Transactional
    public void saveInquiry(String title, String content, Long userId) {
        Inquiry inquiry = Inquiry.builder()
                .userId(userId)
                .inquiryTitle(title)
                .inquiryContent(content)
                .build();
        inquiryRepository.save(inquiry);
    }

    // 내 문의 내역 가져오기
    public List<Inquiry> getMyInquiries(Long userId) {
        return inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 특정 문의 하나 상세 보기
    public Inquiry getInquiryDetail(Long inquiryId) {
        return inquiryRepository.findById(inquiryId).orElse(null);
    }
}