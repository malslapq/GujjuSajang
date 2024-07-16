package com.GujjuSajang.member.seller.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.member.seller.dto.ProductStockUpdateDto;
import com.GujjuSajang.member.seller.dto.SellerDto;
import com.GujjuSajang.member.seller.event.SellerEventProducer;
import com.GujjuSajang.member.seller.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "판매자 등록",
            description = "새로운 판매자를 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "판매자 등록 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PostMapping
    public ResponseEntity<SellerDto> createSeller(
            @RequestBody SellerDto sellerDto,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(sellerService.createSeller(tokenMemberInfo.getId(), sellerDto));
    }

    @Operation(summary = "재고 수정",
            description = "특정 제품의 재고를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "재고 수정 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PatchMapping("/product/{product-id}/stock")
    public ResponseEntity<ProductStockUpdateDto> updateStock(
            @Parameter(description = "제품 ID", example = "1") @PathVariable("product-id") Long productId,
            @RequestBody ProductStockUpdateDto productStockUpdateDto,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(sellerEventProducer.updateProductStock(tokenMemberInfo, productId, productStockUpdateDto));
    }

    @Operation(summary = "예약 제품 판매 시간 등록",
            description = "특정 제품의 예약 판매 시간을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "판매 시간 등록 성공")
            })
    @PostMapping("/product/{product-id}")
    public ResponseEntity<?> setProductSalesTime(
            @Parameter(description = "제품 ID", example = "1") @PathVariable("product-id") Long productId,
            @Parameter(description = "판매 시작 시간", example = "2023-07-16T10:00:00") @RequestParam String startTime,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        sellerEventProducer.setProductSalesTime(tokenMemberInfo, productId, LocalDateTime.parse(startTime));
        return ResponseEntity.ok().body("판매 시간 등록 요청 성공");
    }


}

