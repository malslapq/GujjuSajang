package com.GujjuSajang.cart.controller;

import com.GujjuSajang.cart.dto.UpdateCartProductDto;
import com.GujjuSajang.cart.service.CartService;
import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 제품 담기
    @PostMapping("/add")
    public ResponseEntity<CartDto> addCartProduct(@RequestBody CartProductsDto cartProductsDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.addCartProduct(tokenMemberInfo.getId(), cartProductsDto));
    }

    // 조회
    @GetMapping
    public ResponseEntity<CartDto> getCart(HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.getCart(tokenMemberInfo.getId()));
    }

    // 장바구니 제품 수량 변경
    @PatchMapping("/products/{product-id}")
    public ResponseEntity<CartDto> updateCart(
            @PathVariable("product-id") Long productId,
            @RequestBody UpdateCartProductDto updateCartProductDto,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.updateCart(tokenMemberInfo.getId(), productId, updateCartProductDto));
    }

    // 장바구니 제품 삭제
    @DeleteMapping("/products/{product-id}")
    public ResponseEntity<CartDto> deleteCartProduct(
            @PathVariable("product-id") Long productId,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.deleteCartProduct(tokenMemberInfo.getId(), productId));
    }

    // 장바구니 비우기
    @DeleteMapping("/clear")
    public void deleteCart(HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        cartService.deleteCart(tokenMemberInfo.getId());
    }

}
