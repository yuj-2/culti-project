package com.culti.content.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.content.entity.Content;

public interface ContentRepository extends JpaRepository<Content, Long> {

}
