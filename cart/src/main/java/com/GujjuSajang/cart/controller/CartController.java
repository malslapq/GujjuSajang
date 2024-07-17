package com.GujjuSajang.cart.controller;

import com.GujjuSajang.cart.dto.UpdateCartProductDto;
import com.GujjuSajang.cart.service.CartService;
import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니에 제품 담기",
            description = "장바구니에 제품을 추가합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "제품 추가 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/add")
    public ResponseEntity<CartDto> addCartProduct(
            @RequestBody CartProductsDto cartProductsDto,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.addCartProduct(tokenMemberInfo.getId(), cartProductsDto));
    }

    @Operation(summary = "장바구니 조회",
            description = "장바구니 내용을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "장바구니 조회 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @GetMapping
    public ResponseEntity<CartDto> getCart(HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.getCart(tokenMemberInfo.getId()));
    }

    @Operation(summary = "장바구니 제품 수량 변경",
            description = "장바구니에 담긴 특정 제품의 수량을 변경합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수량 변경 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PatchMapping("/products/{product-id}")
    public ResponseEntity<CartDto> updateCart(
            @Parameter(description = "제품 ID", example = "1") @PathVariable("product-id") Long productId,
            @RequestBody UpdateCartProductDto updateCartProductDto,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.updateCart(tokenMemberInfo.getId(), productId, updateCartProductDto));
    }

    @Operation(summary = "장바구니 제품 삭제",
            description = "장바구니에서 특정 제품을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "제품 삭제 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @DeleteMapping("/products/{product-id}")
    public ResponseEntity<CartDto> deleteCartProduct(
            @Parameter(description = "제품 ID", example = "1") @PathVariable("product-id") Long productId,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(cartService.deleteCartProduct(tokenMemberInfo.getId(), productId));
    }

    @Operation(summary = "장바구니 비우기",
            description = "장바구니의 모든 제품을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "장바구니 비우기 성공")
            })
    @DeleteMapping("/clear")
    public void deleteCart(HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        cartService.deleteCart(tokenMemberInfo.getId());
    }

}
