package com.GujjuSajang.cart.controller;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.cart.dto.CartDto;
import com.GujjuSajang.cart.dto.CartProductsDto;
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

    @GetMapping("/{id}")
    public ResponseEntity<CartDto> getCart(@PathVariable Long id, HttpServletRequest request) {
        TokenMemberInfo tokenUserInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.getCart(id, tokenUserInfo.getId()));
    }

    @PostMapping
    public ResponseEntity<CartDto> addCartProduct(@RequestBody CartProductsDto cartProductsDto, HttpServletRequest request) {
        TokenMemberInfo tokenUserInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.addCartProduct(tokenUserInfo.getId(), cartProductsDto));
    }

    @PatchMapping
    public ResponseEntity<CartDto> updateCart(@RequestBody UpdateCartProductDto updateCartProductDto, HttpServletRequest request) {
        TokenMemberInfo tokenUserInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.updateCart(tokenUserInfo.getId(), updateCartProductDto));
    }

    @DeleteMapping("/{id}")
    public void deleteCart(@PathVariable Long id, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        cartService.deleteCart(id, tokenMemberInfo.getId());
    }

    private TokenMemberInfo getTokenMemberInfo(HttpServletRequest request) {
        return (TokenMemberInfo) request.getAttribute("tokenUserInfo");
    }

}
