package com.GujjuSajang.product.service;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.MemberException;
import com.GujjuSajang.exception.ProductException;
import com.GujjuSajang.member.type.MemberRole;
import com.GujjuSajang.product.dto.ProductDto;
import com.GujjuSajang.product.dto.ProductPageDto;
import com.GujjuSajang.product.entity.Product;
import com.GujjuSajang.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDto createProduct(TokenMemberInfo tokenMemberInfo, ProductDto productDto) {

        memberRoleCheck(tokenMemberInfo.getRole());

        return ProductDto.from(
                productRepository.save(Product.builder()
                        .sellerId(tokenMemberInfo.getId())
                        .name(productDto.getName())
                        .price(productDto.getPrice())
                        .description(productDto.getDescription())
                        .build()
                )
        );
    }

    public ProductPageDto getProducts(Pageable pageable, String keyword) {

        Page<Product> products;
        if (!StringUtils.hasText(keyword)) {
            products = productRepository.findAll(pageable);
        } else {
            products = productRepository.findByNameContaining(keyword, pageable);
        }

        List<ProductDto> productDtos = products.getContent().stream()
                .map(ProductDto::from)
                .toList();

        return ProductPageDto.builder()
                .products(productDtos)
                .pageNumber(products.getNumber())
                .pageSize(products.getSize())
                .totalCount(products.getTotalElements())
                .totalPage(products.getTotalPages())
                .last(products.isLast())
                .build();
    }

    public ProductDto getProduct(long id) {
        return ProductDto.from(productRepository.findById(id).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT)));
    }

    private void memberRoleCheck(MemberRole role) {
        if (!role.equals(MemberRole.SELLER)) {
            throw new MemberException(ErrorCode.ROLE_NOT_ALLOWED);
        }
    }
}
