package com.culti.booking.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity

@Getter @Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})


@Table(name = "place")

public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 255)
    private String address;
    
    // 추가
    @JsonIgnore
    @OneToMany(mappedBy = "place")
    private List<Seat> seats;
}