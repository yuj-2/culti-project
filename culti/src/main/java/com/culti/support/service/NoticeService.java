package com.culti.support.service;

// ✅ 중요: 반드시 우리 프로젝트의 엔티티를 임포트해야 합니다.
import com.culti.support.entity.Notice; 
import com.culti.support.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 최근 공지사항 5개를 가져오는 로직
     */
    public List<Notice> getLatestNotices() {
        // noticeId 기준 내림차순 정렬하여 첫 번째 페이지의 5개 데이터를 가져옴
        return noticeRepository.findAll(
            PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "noticeId"))
        ).getContent();
    }
    
    
    // 페이징된 목록 가져오기
    public Page<Notice> getNoticeList(Pageable pageable) {
        return noticeRepository.findAll(pageable);
    }

    // 상세보기 및 조회수 증가
    @Transactional
    public Notice getNoticeDetail(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공지사항이 없습니다. id=" + id));
        
        notice.setViewCount(notice.getViewCount() + 1); // 조회수 증가
        return notice;
    }
    
}