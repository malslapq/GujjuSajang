package com.GujjuSajang.product;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.core.type.MemberRole;
import com.GujjuSajang.product.dto.CreateProductDto;
import com.GujjuSajang.product.dto.ProductDetailDtoResponse;
import com.GujjuSajang.product.dto.ProductDto;
import com.GujjuSajang.product.dto.ProductPageDto;
import com.GujjuSajang.product.entity.Product;
import com.GujjuSajang.product.repository.ProductRepository;
import com.GujjuSajang.product.service.ProductService;
import com.GujjuSajang.product.stock.entity.Stock;
import com.GujjuSajang.product.stock.repository.StockRedisRepository;
import com.GujjuSajang.product.stock.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private StockRedisRepository stockRedisRepository;
    @InjectMocks
    private ProductService productService;

    Product product;
    CreateProductDto.Request createProductRequest;
    ProductPageDto productPageDto;
    TokenMemberInfo tokenMemberInfo;
    ProductDto productDto;
    Stock stock;

    @BeforeEach
    void init() {
        product = Product.builder()
                .id(1L)
                .sellerId(1L)
                .name("제품")
                .price(1000)
                .description("상세")
                .build();
        createProductRequest = CreateProductDto.Request.builder()
                .name("제품")
                .price(1000)
                .description("상세")
                .build();
        productDto = ProductDto.builder()
                .id(1L)
                .name("제품")
                .price(1000)
                .build();
        productPageDto = ProductPageDto.builder()
                .pageNumber(1)
                .pageSize(5)
                .totalCount(1)
                .totalPage(1)
                .build();
        tokenMemberInfo = TokenMemberInfo.builder()
                .id(1L)
                .mail("mail@example.com")
                .mailVerified(true)
                .role(MemberRole.SELLER)
                .build();
        stock = Stock.builder()
                .id(1L)
                .productId(1L)
                .count(0)
                .build();
    }

    @DisplayName("제품 등록 성공")
    @Test
    public void product_Create_Success() {
        //given
        given(productRepository.save(any(Product.class))).willReturn(product);
        given(stockRepository.save(any(Stock.class))).willReturn(stock);

        //when
        CreateProductDto.Response response = productService.createProduct(tokenMemberInfo, createProductRequest);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(product.getName());
        assertThat(response.getId()).isEqualTo(product.getId());
        assertThat(response.getDescription()).isEqualTo(product.getDescription());
    }

    @DisplayName("제품 등록 실패 - 권한 없음")
    @Test
    public void product_Create_Fail_RoleNotAllow() {
        //given
        TokenMemberInfo roleToken = TokenMemberInfo.builder().role(MemberRole.MEMBER).build();

        //when


        //then
        assertThatThrownBy(() -> productService.createProduct(roleToken, createProductRequest))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.ROLE_NOT_ALLOWED.getErrorMessage());
    }

    @DisplayName("제품 검색 성공")
    @Test
    public void getProduct_Success() {
        //given
        Page<Product> productsPage = new PageImpl<>(List.of(product), PageRequest.of(0, 5), 1);
        given(productRepository.findByNameContaining(any(), any(Pageable.class))).willReturn(productsPage);
        given(productRepository.findAll(any(Pageable.class))).willReturn(productsPage);

        // when
        ProductPageDto resultWithKeyword = productService.getProducts(PageRequest.of(0, 5), "제품");
        ProductPageDto resultWithoutKeyword = productService.getProducts(PageRequest.of(0, 5), "");

        // then
        assertThat(resultWithKeyword).isNotNull();
        assertThat(resultWithKeyword.getProducts()).hasSize(1);
        assertThat(resultWithKeyword.getProducts().get(0).getName()).isEqualTo(product.getName());
        assertThat(resultWithoutKeyword).isNotNull();
        assertThat(resultWithoutKeyword.getProducts()).hasSize(1);
        assertThat(resultWithoutKeyword.getProducts().get(0).getName()).isEqualTo(product.getName());
    }


    @DisplayName("제품 상세 조회 성공")
    @Test
    public void getProductDetail_Success() {
        // given
        given(productRepository.findById(any())).willReturn(Optional.of(product));
        given(stockRepository.findByProductId(any())).willReturn(Optional.of(stock));

        // when
        ProductDetailDtoResponse response = productService.getProduct(product.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(product.getName());
        assertThat(response.getDescription()).isEqualTo(product.getDescription());
        assertThat(response.getCount()).isEqualTo(stock.getCount());
    }

    @DisplayName("제품 상세 조회 실패 - 제품을 찾을 수 없음")
    @Test
    public void getProductDetail_Fail_NotFoundProduct() {
        // given
        given(productRepository.findById(any())).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> productService.getProduct(product.getId()))
                .isInstanceOf(ProductException.class)
                .hasMessage(ErrorCode.NOT_FOUND_PRODUCT.getErrorMessage());
    }

    @DisplayName("제품 상세 조회 실패 - 재고 정보를 찾을 수 없음")
    @Test
    public void getProductDetail_Fail_NotFoundStock() {
        // given
        given(productRepository.findById(any())).willReturn(Optional.of(product));
        given(stockRepository.findByProductId(any())).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> productService.getProduct(product.getId()))
                .isInstanceOf(ProductException.class)
                .hasMessage(ErrorCode.NOT_FOUND_STOCK.getErrorMessage());
    }

}
