package com.GujjuSajang.cart.controller;

import com.GujjuSajang.cart.dto.CartDto;
import com.GujjuSajang.cart.dto.CartProductsDto;
import com.GujjuSajang.cart.dto.TokenMemberInfo;
import com.GujjuSajang.cart.dto.UpdateCartProductDto;
import com.GujjuSajang.cart.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/cart")
@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 제품 담기
    @PostMapping("/{member-id}")
    public ResponseEntity<CartDto> addCartProduct(
            @PathVariable("member-id") Long memberId,
            @RequestBody CartProductsDto cartProductsDto,
            HttpServletRequest request) {
        return ResponseEntity.ok(cartService.addCartProduct(memberId, getTokenMemberInfo(request).getId(), cartProductsDto));
    }

    // 조회
    @GetMapping("/{member-id}")
    public ResponseEntity<CartDto> getCart(@PathVariable("member-id") Long memberId, HttpServletRequest request) {
        return ResponseEntity.ok(cartService.getCart(memberId, getTokenMemberInfo(request).getId()));
    }

    // 장바구니 제품 수량 변경
    @PatchMapping("/{member-id}/products/{product-id}")
    public ResponseEntity<CartDto> updateCart(
            @PathVariable("member-id") Long memberId,
            @PathVariable("product-id") Long productId,
            @RequestBody UpdateCartProductDto updateCartProductDto,
            HttpServletRequest request) {
        return ResponseEntity.ok(cartService.updateCart(memberId, productId, getTokenMemberInfo(request).getId(), updateCartProductDto));
    }

    // 장바구니 제품 삭제
    @DeleteMapping("/{member-id}/products/{product-id}")
    public ResponseEntity<CartDto> deleteCartProduct(
            @PathVariable("member-id") Long memberId,
            @PathVariable("product-id") Long productId,
            HttpServletRequest request) {
        return ResponseEntity.ok(cartService.deleteCartProduct(memberId, productId, getTokenMemberInfo(request).getId()));
    }

    // 장바구니 비우기
    @DeleteMapping("/{member-id}")
    public void deleteCart(@PathVariable("member-id") Long memberId, HttpServletRequest request) {
        cartService.deleteCart(memberId, getTokenMemberInfo(request).getId());
    }

    public static TokenMemberInfo getTokenMemberInfo(HttpServletRequest request) {
        return (TokenMemberInfo) request.getAttribute("tokenMemberInfo");
    }

}
