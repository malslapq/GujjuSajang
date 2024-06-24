package com.GujjuSajang.seller.controller;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.seller.dto.ProductStockUpdateDto;
import com.GujjuSajang.seller.dto.SellerDto;
import com.GujjuSajang.seller.service.SellerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.GujjuSajang.Jwt.util.JwtUtil.getTokenMemberInfo;

@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    // 판매자 등록
    @PostMapping()
    public ResponseEntity<SellerDto> createSeller(@RequestBody SellerDto sellerDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok(sellerService.createSeller(tokenMemberInfo.getId(), sellerDto));
    }

    // 재고 수정
    @PatchMapping("/{seller-id}/product/{product-id}/stock")
    public ResponseEntity<ProductStockUpdateDto> updateStock(@PathVariable("seller-id") Long sellerId,
                                                             @PathVariable("product-id") Long productId,
                                                             @RequestBody ProductStockUpdateDto productStockUpdateDto,
                                                             HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok(sellerService.updateProductStock(sellerId, tokenMemberInfo, productId, productStockUpdateDto));
    }


}
