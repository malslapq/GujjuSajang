package com.GujjuSajang.orders.product.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.orders.product.dto.OrdersProductDto;
import com.GujjuSajang.orders.product.event.OrdersProductEventProducer;
import com.GujjuSajang.orders.product.service.OrdersProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "주문 제품 API", description = "주문 제품 관련 API")
@RestController
@RequiredArgsConstructor
public class OrdersProductController {

    private final OrdersProductService ordersProductService;
    private final OrdersProductEventProducer ordersProductEventProducer;

    @Operation(summary = "특정 주문의 제품들 조회",
            description = "특정 주문에 포함된 제품 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주문 제품 목록 조회 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/{order-id}/products")
    public ResponseEntity<List<OrdersProductDto>> getOrderProducts(
            @Parameter(description = "주문 ID", example = "1") @PathVariable("order-id") Long orderId,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersProductService.getOrderProducts(tokenMemberInfo.getId(), orderId));
    }

    @Operation(summary = "주문 제품 취소",
            description = "특정 주문 제품을 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주문 제품 취소 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PatchMapping("/product/{order-product-id}/cancel")
    public ResponseEntity<OrdersProductDto> cancelOrderProduct(
            @Parameter(description = "주문 제품 ID", example = "1") @PathVariable("order-product-id") Long orderProductId,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersProductEventProducer.cancelOrderProduct(tokenMemberInfo.getId(), orderProductId));
    }

    @Operation(summary = "주문 제품 반품",
            description = "특정 주문 제품을 반품합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주문 제품 반품 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PatchMapping("/product/{order-product-id}/return")
    public ResponseEntity<OrdersProductDto> returnOrderProduct(
            @Parameter(description = "주문 제품 ID", example = "1") @PathVariable("order-product-id") Long orderProductId,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersProductService.returnOrderProduct(tokenMemberInfo.getId(), orderProductId));
    }

}
