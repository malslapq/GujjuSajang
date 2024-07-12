package com.GujjuSajang.orders.controller;

import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.orders.dto.OrdersPageDto;
import com.GujjuSajang.orders.event.OrdersEventProducer;
import com.GujjuSajang.orders.service.OrdersService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;
    private final OrdersEventProducer ordersEventProducer;

    // 주문 요청
    @PostMapping
    public ResponseEntity<?> createOrder(HttpServletRequest request, @RequestBody CartDto cartDto) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        ordersEventProducer.createOrder(tokenMemberInfo.getId(), cartDto);
        return ResponseEntity.ok().body("주문 요청");
    }

    // 주문 조회
    @GetMapping
    public ResponseEntity<OrdersPageDto> getOrder(@RequestParam int page, @RequestParam int size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersService.getOrder(tokenMemberInfo.getId(), pageable));
    }

    // 선착순 주문
    @PostMapping("/firstcome")
    public ResponseEntity<?> firstComeOrders(HttpServletRequest request, @RequestBody CartProductsDto cartProductsDto) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        ordersEventProducer.createFirstComeOrders(tokenMemberInfo.getId(), cartProductsDto);
        return ResponseEntity.ok().body("선착순 주문 요청");
    }


}