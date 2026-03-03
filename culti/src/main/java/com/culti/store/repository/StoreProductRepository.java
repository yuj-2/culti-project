package com.culti.store.repository;

import com.culti.store.entity.StoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {
    
    // 카테고리 ID로 판매 중인 상품 조회 (JPA 메서드 쿼리)
    List<StoreProduct> findBysCatIdAndIsActive(Long sCatId, String isActive);
    
    // 베스트 상품 우선 정렬 조회
    List<StoreProduct> findBysCatIdAndIsActiveOrderByIsBestDescProdIdAsc(Long sCatId, String isActive);
}