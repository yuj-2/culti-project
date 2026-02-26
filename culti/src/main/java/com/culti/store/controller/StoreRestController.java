package com.culti.store.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.culti.store.dto.StoreProductDTO;
import com.culti.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/store/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 모든 도메인에서의 요청을 허용 (보안 설정을 건드리지 않고 해결)
public class StoreRestController {

    private final StoreService storeService;

    @GetMapping("/products")
    public List<StoreProductDTO> getProducts(@RequestParam("sCatId") Long sCatId) {
        // 비동기 요청(fetch)에 응답하여 JSON 데이터 반환
        return storeService.getProductsByCategory(sCatId);
    }
}