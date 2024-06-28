package com.GujjuSajang.orders.controller;

import com.GujjuSajang.orders.service.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrdersController {

    private final OrdersService ordersService;

//    // 주문 (결제)
//    @PostMapping("/{member-id}")
//    public ResponseEntity<OrdersDto> createOrder(@PathVariable("member-id") Long memberId, HttpServletRequest request) {
//        return ResponseEntity.ok(ordersService.createOrder(memberId, tokenMemberInfo.getId()));
//    }
//
//    // 주문 조회
//    @GetMapping("/{member-id}")
//    public ResponseEntity<OrdersPageDto> getOrder(@PathVariable("member-id") Long memberId, @RequestParam int page, @RequestParam int size, HttpServletRequest request) {
//        Pageable pageable = PageRequest.of(page, size);
//        return ResponseEntity.ok(ordersService.getOrder(memberId, tokenMemberInfo.getId(), pageable));
//    }
//
//    // 특정 주문의 제품들 조회
//    @GetMapping("/{order-id}/products")
//    public ResponseEntity<List<OrdersProductDto>> getOrderProducts(@PathVariable("order-id") Long orderId) {
//        return ResponseEntity.ok(ordersService.getOrderProducts(orderId));
//    }
//
//    // 주문 취소
//    @PatchMapping("/{member-id}/product/{order-product-id}/cancel")
//    public ResponseEntity<OrdersProductDto> cancelOrderProduct(@PathVariable("member-id") Long memberId, @PathVariable("order-product-id") Long orderProductId, HttpServletRequest request) {
//        return ResponseEntity.ok(ordersService.cancelOrderProduct(memberId, orderProductId, tokenMemberInfo.getId()));
//    }
//
//    // 주문 반품
//    @PatchMapping("/{member-id}/product/{order-product-id}/return")
//    public ResponseEntity<OrdersProductDto> returnOrderProduct(@PathVariable("member-id") Long memberId, @PathVariable("order-product-id") Long orderProductId, HttpServletRequest request) {
//        return ResponseEntity.ok(ordersService.returnOrderProduct(memberId, orderProductId, tokenMemberInfo.getId()));
//    }


}