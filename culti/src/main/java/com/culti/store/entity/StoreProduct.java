package com.culti.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "store_product")
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prodId;

    @Column(nullable = false)
    private Long sCatId;

    @Column(nullable = false, length = 100)
    private String name;

    private String subText;

    @Column(nullable = false)
    private int price;

    private String imgUrl;

    @Column(columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String isBest;

    @Column(columnDefinition = "CHAR(1) DEFAULT 'Y'")
    private String isActive;
}