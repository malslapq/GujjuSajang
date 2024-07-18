package com.GujjuSajang.product.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.product.dto.CreateProductDto;
import com.GujjuSajang.product.dto.ProductDetailDtoResponse;
import com.GujjuSajang.product.dto.ProductPageDto;
import com.GujjuSajang.product.service.ProductService;
import com.GujjuSajang.product.stock.dto.StockDto;
import com.GujjuSajang.product.stock.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "제품 서비스 API", description = "제품 관련 API")
@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final StockService stockService;

    @Operation(summary = "제품 등록",
            description = "판매자가 제품을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "제품 등록 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/product")
    public ResponseEntity<CreateProductDto.Response> createProduct(
            @RequestBody CreateProductDto.Request productDetailDtoRequest,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok(productService.createProduct(tokenMemberInfo, productDetailDtoRequest));
    }

    @Operation(summary = "제품 상세 조회",
            description = "제품의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "제품 상세 정보 조회 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/{product-id}")
    public ResponseEntity<ProductDetailDtoResponse> getProduct(
            @Parameter(description = "조회할 제품의 ID", example = "1") @PathVariable("product-id") Long productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    @Operation(summary = "제품 검색",
            description = "키워드를 통해 제품을 검색합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "제품 검색 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/list")
    public ResponseEntity<ProductPageDto> getProducts(
            @Parameter(description = "페이지 번호", example = "0") @RequestParam int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam int size,
            @Parameter(description = "검색 키워드", example = "신발") @RequestParam String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProducts(pageable, keyword));
    }

    @Operation(summary = "제품 재고 조회",
            description = "제품의 재고 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "제품 재고 정보 조회 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/{product-id}/stock")
    public ResponseEntity<StockDto> getStock(
            @Parameter(description = "조회할 제품의 ID", example = "1") @PathVariable("product-id") Long productId) {
        return ResponseEntity.ok().body(stockService.getStock(productId));
    }


}
