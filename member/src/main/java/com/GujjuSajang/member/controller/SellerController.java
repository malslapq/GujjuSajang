package com.GujjuSajang.member.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.member.dto.ProductStockUpdateDto;
import com.GujjuSajang.member.dto.SellerDto;
import com.GujjuSajang.member.event.SellerEventProducer;
import com.GujjuSajang.member.service.SellerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}

