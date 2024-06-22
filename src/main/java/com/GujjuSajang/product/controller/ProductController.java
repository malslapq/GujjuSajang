package com.GujjuSajang.product.controller;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.product.dto.ProductDto;
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

    @PostMapping("/product")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto, HttpServletRequest request) {
        return ResponseEntity.ok(productService.createProduct(getTokenMemberInfo(request), productDto));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/products")
    public ResponseEntity<ProductPageDto> getProducts(@RequestParam int page, @RequestParam int size, @RequestParam String keyword, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProducts(pageable, keyword));
    }

    private TokenMemberInfo getTokenMemberInfo(HttpServletRequest request) {
        return (TokenMemberInfo) request.getAttribute("tokenUserInfo");
    }

}
