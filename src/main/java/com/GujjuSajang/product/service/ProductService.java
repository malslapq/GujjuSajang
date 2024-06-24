package com.GujjuSajang.product.service;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.MemberException;
import com.GujjuSajang.exception.ProductException;
import com.GujjuSajang.member.type.MemberRole;
import com.GujjuSajang.product.dto.ProductDetailDto;
import com.GujjuSajang.product.dto.ProductDto;
import com.GujjuSajang.product.dto.ProductPageDto;
import com.GujjuSajang.product.entity.Product;
import com.GujjuSajang.product.repository.ProductRepository;
import com.GujjuSajang.product.stock.Entity.Stock;
import com.GujjuSajang.product.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    // 제품 등록
    @Transactional
    public ProductDetailDto createProduct(Long sellerId, TokenMemberInfo tokenMemberInfo, ProductDetailDto productDetailDto) {

        memberRoleCheck(tokenMemberInfo.getRole());
        productDetailDto.setSellerId(sellerId);

        Product product = productRepository.save(Product.from(productDetailDto));

        return ProductDetailDto.from(product,
                stockRepository.save(Stock.builder()
                        .productId(product.getId())
                        .count(0)
                        .build()));
    }

    // 제품 검색
    @Transactional(readOnly = true)
    public ProductPageDto getProducts(Pageable pageable, String keyword) {

        Page<Product> products = StringUtils.hasText(keyword)
                ? productRepository.findByNameContaining(keyword, pageable)
                : productRepository.findAll(pageable);

        List<ProductDto> productDetailDtos = products.getContent().stream()
                .map(ProductDto::from)
                .toList();

        return ProductPageDto.builder()
                .products(productDetailDtos)
                .pageNumber(products.getNumber())
                .pageSize(products.getSize())
                .totalCount(products.getTotalElements())
                .totalPage(products.getTotalPages())
                .last(products.isLast())
                .build();
    }

    // 상품 상세 조회
    @Transactional(readOnly = true)
    public ProductDetailDto getProduct(long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));
        Stock stock = stockRepository.findByProductId(product.getId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_STOCK));
        return ProductDetailDto.from(product, stock);
    }


    private void memberRoleCheck(MemberRole role) {
        if (!role.equals(MemberRole.SELLER)) {
            throw new MemberException(ErrorCode.ROLE_NOT_ALLOWED);
        }
    }
}
