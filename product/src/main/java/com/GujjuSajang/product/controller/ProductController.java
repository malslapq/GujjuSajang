package com.GujjuSajang.product.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.product.dto.CreateProductDto;
import com.GujjuSajang.product.dto.ProductDetailDtoResponse;
import com.GujjuSajang.product.dto.ProductPageDto;
import com.GujjuSajang.product.service.ProductService;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.service.StockService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final StockService stockService;

    // 제품 등록
    @PostMapping
    public ResponseEntity<CreateProductDto.Response> createProduct(@RequestBody CreateProductDto.Request productDetailDtoRequest, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(productService.createProduct(tokenMemberInfo, productDetailDtoRequest));
    }

    // 제품 상세 조회
    @GetMapping("/{product-id}")
    public ResponseEntity<ProductDetailDtoResponse> getProduct(@PathVariable("product-id") Long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    // 제품 검색
    @GetMapping("/list")
    public ResponseEntity<ProductPageDto> getProducts(@RequestParam int page, @RequestParam int size, @RequestParam String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProducts(pageable, keyword));
    }

    // 제품 재고 조회
    @GetMapping("/{product-id}/stock")
    public ResponseEntity<StockDto> getStock(@PathVariable("product-id") Long productId) {
        return ResponseEntity.ok().body(stockService.getStock(productId));
    }


}
