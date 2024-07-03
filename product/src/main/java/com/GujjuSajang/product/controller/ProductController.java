package com.GujjuSajang.product.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
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
    @PostMapping
    public ResponseEntity<ProductDetailDto> createProduct(@RequestBody ProductDetailDto productDetailDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(productService.createProduct(tokenMemberInfo, productDetailDto));
    }

    // 제품 상세 조회
    @GetMapping("/{product-id}")
    public ResponseEntity<ProductDetailDto> getProduct(@PathVariable("product-id") Long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    // 제품 검색
    @GetMapping("/list")
    public ResponseEntity<ProductPageDto> getProducts(@RequestParam int page, @RequestParam int size, @RequestParam String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProducts(pageable, keyword));
    }


}
