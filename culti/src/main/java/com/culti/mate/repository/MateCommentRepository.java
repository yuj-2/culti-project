package com.culti.mate.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.culti.mate.entity.MateComment;
import com.culti.mate.entity.MatePost;

public interface MateCommentRepository extends JpaRepository<MateComment, Long> {

    List<MateComment> findByPostOrderByCreatedAtAsc(MatePost post);
    void deleteByPost_PostId(Long postId);  
}