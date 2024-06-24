package com.GujjuSajang.order.controller;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.order.dto.OrdersDto;
import com.GujjuSajang.order.dto.OrdersProductDto;
import com.GujjuSajang.order.dto.OrdersPageDto;
import com.GujjuSajang.order.service.OrdersService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.GujjuSajang.Jwt.util.JwtUtil.getTokenMemberInfo;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

    // 주문 (결제)
    @PostMapping("/{member-id}")
    public ResponseEntity<OrdersDto> createOrder(@PathVariable("member-id") Long memberId, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok(ordersService.createOrder(memberId, tokenMemberInfo.getId()));
    }

    // 주문 조회
    @GetMapping("/{member-id}")
    public ResponseEntity<OrdersPageDto> getOrder(@PathVariable("member-id") Long memberId, @RequestParam int page, @RequestParam int size, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ordersService.getOrder(memberId, tokenMemberInfo.getId(), pageable));
    }

    // 특정 주문의 제품들 조회
    @GetMapping("/{order-id}/products")
    public ResponseEntity<List<OrdersProductDto>> getOrderProducts(@PathVariable("order-id") Long orderId) {
        return ResponseEntity.ok(ordersService.getOrderProducts(orderId));
    }

    // 주문 취소
    @PatchMapping("/{member-id}/product/{order-product-id}/cancel")
    public ResponseEntity<OrdersProductDto> cancelOrderProduct(@PathVariable("member-id") Long memberId, @PathVariable("order-product-id") Long orderProductId, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok(ordersService.cancelOrderProduct(memberId, orderProductId, tokenMemberInfo.getId()));
    }

    // 주문 반품
    @PatchMapping("/{member-id}/product/{order-product-id}/return")
    public ResponseEntity<OrdersProductDto> returnOrderProduct(@PathVariable("member-id") Long memberId, @PathVariable("order-product-id") Long orderProductId, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok(ordersService.returnOrderProduct(memberId, orderProductId, tokenMemberInfo.getId()));
    }


}