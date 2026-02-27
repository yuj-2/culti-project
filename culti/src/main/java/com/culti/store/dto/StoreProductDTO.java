package com.culti.store.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreProductDTO {
    private Long prodId;      // 상품 ID
    private Long sCatId;      // 카테고리 ID
    private String name;      // 상품명
    private String subText;   // 상품 설명
    private int price;        // 가격
    private String imgUrl;    // 이미지 경로
    private String isBest;    // 베스트 여부
}