package com.culti.mate.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MateCommentDTO {

	private Long commentId;

    private Long postId;

    private String content;

    private String writerNickname;

    private String writerEmail;   // 수정/삭제 권한 판단용 

    private LocalDateTime createdAt;
	
}
