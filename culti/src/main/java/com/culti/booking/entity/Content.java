package com.culti.booking.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@Table(name = "content")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contentId;

    private String category;
    private String title;
    private String posterUrl;
    private String description;
    private String ageLimit;
    private Integer runningTime;
    private LocalDateTime createdAt;
}