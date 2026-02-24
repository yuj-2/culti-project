package com.culti.support.repository;

import com.culti.support.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    // JpaRepository를 상속받으면 기본적인 CRUD 로직이 자동으로 생성됩니다.
}