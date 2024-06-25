package com.GujjuSajang.service;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.MemberException;
import com.GujjuSajang.exception.ProductException;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.type.MemberRole;
import com.GujjuSajang.product.entity.Product;
import com.GujjuSajang.product.stock.Entity.Stock;
import com.GujjuSajang.product.stock.repository.StockRepository;
import com.GujjuSajang.seller.dto.ProductStockUpdateDto;
import com.GujjuSajang.seller.dto.SellerDto;
import com.GujjuSajang.seller.entity.Seller;
import com.GujjuSajang.seller.repository.SellerRepository;
import com.GujjuSajang.seller.service.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
public class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    SellerService sellerService;

    Member member;
    ProductStockUpdateDto productStockUpdateDto;
    TokenMemberInfo tokenMemberInfo;
    Stock stock;
    Seller seller;
    SellerDto sellerDto;
    Product product;

    @BeforeEach
    void init() {
        member = Member.builder()
                .id(1L)
                .name("name")
                .phone("phone")
                .address("address")
                .password("encodedPassword")
                .mail("mail@example.com")
                .mailVerified(false)
                .role(MemberRole.SELLER)
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
                .count(5)
                .build();
        seller = Seller.builder()
                .id(1L)
                .memberId(1L)
                .name("(주) 테스트")
                .address("공장")
                .contactNumber("031-111-222")
                .registrationNumber("12-313-333")
                .build();
        sellerDto = SellerDto.builder()
                .id(1L)
                .memberId(1L)
                .name("(주) 테스트")
                .address("공장")
                .contactNumber("031-111-222")
                .registrationNumber("12-313-333")
                .build();
        productStockUpdateDto = ProductStockUpdateDto.builder()
                .stockId(1L)
                .productId(1L)
                .name("제품")
                .count(5)
                .build();
        product = Product.builder()
                .id(1L)
                .sellerId(1L)
                .name("제품")
                .price(1000)
                .description("설명")
                .build();
    }

    @DisplayName("판매자 등록 성공")
    @Test
    void sellerCreate_Success() {
        //given
        given(memberRepository.findById(member.getId())).willReturn(Optional.ofNullable(member));
        given(sellerRepository.save(any())).willReturn(seller);

        //when
        SellerDto result = sellerService.createSeller(member.getId(), sellerDto);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sellerDto.getId());
        assertThat(result.getMemberId()).isEqualTo(member.getId());
        assertThat(result.getName()).isEqualTo(sellerDto.getName());
        assertThat(result.getAddress()).isEqualTo(sellerDto.getAddress());
        assertThat(result.getContactNumber()).isEqualTo(sellerDto.getContactNumber());
        assertThat(result.getRegistrationNumber()).isEqualTo(sellerDto.getRegistrationNumber());
    }

    @DisplayName("판매자 등록 실패 - 회원을 찾을 수 없음")
    @Test
    void sellerCreate_Fail_NotFoundMember() {
        //given

        //when

        //then
        assertThatThrownBy(() -> sellerService.createSeller(member.getId(), sellerDto))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.NOT_FOUND_MEMBER.getErrorMessage());
    }


    @DisplayName("제품 재고 변경 성공")
    @Test
    void updateProductStock_Success() {
        //given
        given(stockRepository.findByProductId(anyLong())).willReturn(Optional.ofNullable(stock));

        //when
        ProductStockUpdateDto result = sellerService.updateProductStock(seller.getId(), tokenMemberInfo, product.getId(), productStockUpdateDto);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(product.getId());
        assertThat(result.getStockId()).isEqualTo(stock.getId());
        assertThat(result.getCount()).isEqualTo(stock.getCount());
        assertThat(result.getName()).isEqualTo(product.getName());

    }

    @DisplayName("제품 재고 변경 실패 - 재고 정보를 찾을 수 없음")
    @Test
    void updateProductStock_Fail_NotFoundStock() {
        //given
        given(stockRepository.findByProductId(product.getId())).willReturn(Optional.empty());

        //when

        //then
        assertThatThrownBy(() -> sellerService.updateProductStock(seller.getId(), tokenMemberInfo, product.getId(), productStockUpdateDto))
                .isInstanceOf(ProductException.class)
                .hasMessage(ErrorCode.NOT_FOUND_STOCK.getErrorMessage());

    }

    @DisplayName("제품 재고 변경 실패 - 권한이 없음")
    @Test
    void updateProductStock_Fail_RoleNotAllow() {
        //given
        tokenMemberInfo = TokenMemberInfo.builder().role(MemberRole.MEMBER).build();

        //when

        //then
        assertThatThrownBy(() -> sellerService.updateProductStock(seller.getId(), tokenMemberInfo, product.getId(), productStockUpdateDto))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.ROLE_NOT_ALLOWED.getErrorMessage());
    }


}
