package com.GujjuSajang.orders.product.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.orders.product.dto.OrdersProductDto;
import com.GujjuSajang.orders.product.event.OrdersProductEventProducer;
import com.GujjuSajang.orders.product.service.OrdersProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrdersProductController {

    private final OrdersProductService ordersProductService;
    private final OrdersProductEventProducer ordersProductEventProducer;

    // 특정 주문의 제품들 조회
    @GetMapping("/{order-id}/products")
    public ResponseEntity<List<OrdersProductDto>> getOrderProducts(@PathVariable("order-id") Long orderId, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersProductService.getOrderProducts(tokenMemberInfo.getId(), orderId));
    }

    // 주문 취소
    @PatchMapping("/product/{order-product-id}/cancel")
    public ResponseEntity<OrdersProductDto> cancelOrderProduct(@PathVariable("order-product-id") Long orderProductId, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersProductEventProducer.cancelOrderProduct(tokenMemberInfo.getId(), orderProductId));
    }

    // 주문 반품
    @PatchMapping("/product/{order-product-id}/return")
    public ResponseEntity<OrdersProductDto> returnOrderProduct(@PathVariable("order-product-id") Long orderProductId, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersProductService.returnOrderProduct(tokenMemberInfo.getId(), orderProductId));
    }

}
