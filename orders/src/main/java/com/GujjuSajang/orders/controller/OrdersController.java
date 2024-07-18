package com.GujjuSajang.orders.controller;

import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.orders.dto.OrdersPageDto;
import com.GujjuSajang.orders.event.OrdersEventProducer;
import com.GujjuSajang.orders.service.OrdersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "주문 API", description = "주문 관련 API")
@RestController
@RequiredArgsConstructor
public class OrdersController {

    private final OrdersService ordersService;
    private final OrdersEventProducer ordersEventProducer;


    @Operation(summary = "주문 요청",
            description = "장바구니의 항목들을 주문 요청합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주문 요청 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(HttpServletRequest request, @RequestBody CartDto cartDto) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        ordersEventProducer.createOrder(tokenMemberInfo.getId(), cartDto);
        return ResponseEntity.ok().body("주문 요청");
    }

    @Operation(summary = "주문 조회",
            description = "사용자의 주문 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "주문 조회 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/orders")
    public ResponseEntity<OrdersPageDto> getOrder(@RequestParam int page, @RequestParam int size, HttpServletRequest request) {
        Pageable pageable = PageRequest.of(page, size);
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(ordersService.getOrder(tokenMemberInfo.getId(), pageable));
    }

    @Operation(summary = "선착순 주문",
            description = "선착순으로 특정 상품들을 주문합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "선착순 주문 요청 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/firstcome")
    public ResponseEntity<String> firstComeOrders(HttpServletRequest request, @RequestBody CartProductsDto cartProductsDto) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        ordersEventProducer.createFirstComeOrders(tokenMemberInfo.getId(), cartProductsDto);
        return ResponseEntity.ok().body("선착순 주문 요청");
    }


}