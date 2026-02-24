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
    public void saveInquiry(Inquiry inquiry) { // 매개변수가 Inquiry 객체 하나여야 합니다!
        inquiryRepository.save(inquiry);
    }

    public List<Inquiry> getMyInquiries(Long userId) {
        return inquiryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Inquiry getInquiryDetail(Long inquiryId) {
        return inquiryRepository.findById(inquiryId).orElse(null);
    }
}