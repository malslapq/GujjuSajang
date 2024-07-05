package com.GujjuSajang.orders.controller;

import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.orders.dto.OrdersDto;
import com.GujjuSajang.orders.dto.OrdersPageDto;
import com.GujjuSajang.orders.dto.OrdersProductDto;
import com.GujjuSajang.orders.event.OrdersEventProducer;
import com.GujjuSajang.orders.event.OrdersProductEventProducer;
import com.GujjuSajang.orders.service.OrdersProductService;
import com.GujjuSajang.orders.service.OrdersService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;
    private final OrdersProductService ordersProductService;
    private final OrdersEventProducer ordersEventProducer;
    private final OrdersProductEventProducer ordersProductEventProducer;

    // 주문 (결제)
    @PostMapping
    public ResponseEntity<OrdersDto> createOrder(HttpServletRequest request, @RequestBody CartDto cartDto) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersEventProducer.createOrder(tokenMemberInfo.getId(), cartDto));
    }

    // 주문 조회
    @GetMapping
    public ResponseEntity<OrdersPageDto> getOrder(@RequestParam int page, @RequestParam int size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersService.getOrder(tokenMemberInfo.getId(), pageable));
    }

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