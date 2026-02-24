package com.culti.support.service;

// ✅ 중요: 반드시 우리 프로젝트의 엔티티를 임포트해야 합니다.
import com.culti.support.entity.Notice; 
import com.culti.support.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;

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
}