package com.culti.support.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.culti.support.entity.Faq;
import com.culti.support.repository.FaqRepository;

@Service
public class FaqService {

    @Autowired
    private FaqRepository faqRepository;

    public List<Faq> findAll() {
        return faqRepository.findAll(); // 모든 FAQ 데이터 가져오기
    }
    
    public Page<Faq> getFaqList(Pageable pageable) { return faqRepository.findAll(pageable); }
    public Page<Faq> getFaqListByCategory(String category, Pageable pageable) { 
        return faqRepository.findByFaqCategory(category, pageable); 
    }
    
    
}