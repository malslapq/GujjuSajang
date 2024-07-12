package com.GujjuSajang.member.seller.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.member.seller.dto.ProductStockUpdateDto;
import com.GujjuSajang.member.seller.dto.SellerDto;
import com.GujjuSajang.member.seller.event.SellerEventProducer;
import com.GujjuSajang.member.seller.service.SellerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;
    private final SellerEventProducer sellerEventProducer;

    // 판매자 등록
    @PostMapping
    public ResponseEntity<SellerDto> createSeller(@RequestBody SellerDto sellerDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(sellerService.createSeller(tokenMemberInfo.getId(), sellerDto));
    }

    // 재고 수정
    @PatchMapping("/product/{product-id}/stock")
    public ResponseEntity<ProductStockUpdateDto> updateStock(@PathVariable("product-id") Long productId,
                                                             @RequestBody ProductStockUpdateDto productStockUpdateDto,
                                                             HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(sellerEventProducer.updateProductStock(tokenMemberInfo, productId, productStockUpdateDto));
    }

    // 예약 제품 판매 시간 등록
    @PostMapping("/product/{product-id}")
    public ResponseEntity<?> setProductSalesTime(@PathVariable("product-id") Long productId, @RequestParam String startTime, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        sellerEventProducer.setProductSalesTime(tokenMemberInfo, productId, LocalDateTime.parse(startTime));
        return ResponseEntity.ok().body("판매 시간 등록 요청 성공");
    }


}

