package com.culti.mate.DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@Builder
public class MyPostMypageDTO {

	private Long postId;

    private String title;
    private String category;      // enum name (MOVIE 등)
    private String categoryLabel;
    
    private LocalDateTime createdAt;

    private String eventAtText;
    private String location;

    private String status;        // enum name (OPEN/CLOSED 등)
    private String statusLabel;   // 한글 라벨 (모집중/마감 등)
}

