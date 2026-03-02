package com.culti.mate.DTO;

import java.time.LocalDateTime;

import com.culti.mate.enums.MateApplyStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MateApplyDTO {

    private Long id;

    private String postTitle;
    private String location;
    private String eventAtText;

    private String applicantNickname;
    private String writerNickname;

    private MateApplyStatus status;
    private String statusLabel;

    private LocalDateTime createdAt;
}
