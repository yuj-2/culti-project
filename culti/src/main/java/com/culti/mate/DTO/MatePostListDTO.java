package com.culti.mate.DTO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class MatePostListDTO {
  private Long postId;
  private String title;
  private String category;      // "MOVIE" 같은 문자열로
  private LocalDateTime eventAt;
  private String location;
  private Integer maxPeople;
  private String description;

  private String writerNickname;
  private String writerEmail;

  private int acceptedCount;
}