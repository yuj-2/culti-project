package com.culti.mate.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.mate.entity.MatePost;

public interface MateRepository extends JpaRepository<MatePost, Long> {

}
