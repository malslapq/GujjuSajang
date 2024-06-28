package com.GujjuSajang.product.controller;

import com.GujjuSajang.product.dto.ProductDetailDto;
import com.GujjuSajang.product.dto.ProductPageDto;
import com.GujjuSajang.product.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 제품 등록
    @PostMapping("/product/{seller-id}")
    public ResponseEntity<ProductDetailDto> createProduct(
            @PathVariable("seller-id") Long sellerId,
            @RequestBody ProductDetailDto productDetailDto,
            HttpServletRequest request) {
        return ResponseEntity.ok(productService.createProduct(sellerId, productDetailDto));
    }

    // 제품 상세 조회
    @GetMapping("/product/{product-id}")
    public ResponseEntity<ProductDetailDto> getProduct(@PathVariable("product-id") long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    // 제품 검색
    @GetMapping("/products")
    public ResponseEntity<ProductPageDto> getProducts(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String keyword,
            HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProducts(pageable, keyword));
    }


}
