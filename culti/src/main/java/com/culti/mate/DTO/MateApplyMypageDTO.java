package com.culti.mate.DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter 
@Builder
public class MateApplyMypageDTO {
    private Long id;

    private String postTitle;
    private String applicantNickname; // received에서 필요
    private String writerNickname;    // sent에서 필요

    private LocalDateTime createdAt;

    private String eventAtText;
    private String location;

    private String status;      // "PENDING" 같은 enum name
    private String statusLabel; // "대기중" 같은 한글 라벨
}
