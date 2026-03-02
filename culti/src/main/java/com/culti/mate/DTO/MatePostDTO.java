package com.culti.mate.DTO;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.culti.mate.enums.MatePostCategory;
import com.culti.mate.enums.MatePostStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatePostDTO {

    private Long postId;

    private String title;

    private MatePostCategory category;   

    private String date; 
    private String time;
    
    private LocalDateTime eventAt;
    private String location;
    private Integer maxPeople;

    private String description;

    private String writerNickname;

    private MatePostStatus status;   
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
	
}
