package com.culti.store.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.culti.store.dto.StoreProductDTO;
import com.culti.store.entity.StoreProduct;
import com.culti.store.repository.StoreProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreProductRepository storeProductRepository;

    public List<StoreProductDTO> getProductsByCategory(Long sCatId) {
        List<StoreProduct> products = storeProductRepository
                .findBysCatIdAndIsActiveOrderByIsBestDescProdIdAsc(sCatId, "Y");

        // Entity -> DTO 변환 (Stream API 활용)
        return products.stream().map(p -> {
            StoreProductDTO dto = new StoreProductDTO();
            dto.setProdId(p.getProdId());
            dto.setSCatId(p.getSCatId());
            dto.setName(p.getName());
            dto.setSubText(p.getSubText());
            dto.setPrice(p.getPrice());
            dto.setImgUrl(p.getImgUrl());
            dto.setIsBest(p.getIsBest());
            return dto;
        }).collect(Collectors.toList());
    }
}