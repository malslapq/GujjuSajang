package com.GujjuSajang.member.controller;

import com.GujjuSajang.member.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

//    // 판매자 등록
//    @PostMapping()
//    public ResponseEntity<SellerDto> createSeller(@RequestBody SellerDto sellerDto, HttpServletRequest request) {
//        return ResponseEntity.ok(sellerService.createSeller(sellerDto));
//    }
//
//    // 재고 수정
//    @PatchMapping("/{seller-id}/product/{product-id}/stock")
//    public ResponseEntity<ProductStockUpdateDto> updateStock(@PathVariable("seller-id") Long sellerId,
//                                                             @PathVariable("product-id") Long productId,
//                                                             @RequestBody ProductStockUpdateDto productStockUpdateDto,
//                                                             HttpServletRequest request) {
//        return ResponseEntity.ok(sellerService.updateProductStock(sellerId, productId, productStockUpdateDto));
//    }


}
