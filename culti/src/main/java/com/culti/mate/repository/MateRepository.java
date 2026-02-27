package com.culti.mate.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.auth.entity.User;
import com.culti.mate.entity.MateApply;
import com.culti.mate.entity.MatePost;
import com.culti.mate.enums.MateApplyStatus;
import com.culti.mate.enums.MatePostCategory;

public interface MateRepository extends JpaRepository<MatePost, Long> {

	Page<MatePost> findByCategory(MatePostCategory enumCategory, Pageable pageable);

}
