package com.culti.support.service;

import com.culti.support.entity.Faq;
import com.culti.support.repository.FaqRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FaqService {

    @Autowired
    private FaqRepository faqRepository;

    public List<Faq> findAll() {
        return faqRepository.findAll(); // 모든 FAQ 데이터 가져오기
    }
}