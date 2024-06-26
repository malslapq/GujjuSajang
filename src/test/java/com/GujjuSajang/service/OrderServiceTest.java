package com.GujjuSajang.service;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.cart.dto.CartDto;
import com.GujjuSajang.cart.dto.CartProductsDto;
import com.GujjuSajang.cart.repository.CartRedisRepository;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.MemberException;
import com.GujjuSajang.exception.OrdersException;
import com.GujjuSajang.exception.ProductException;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.type.MemberRole;
import com.GujjuSajang.order.dto.OrdersDto;
import com.GujjuSajang.order.dto.OrdersPageDto;
import com.GujjuSajang.order.dto.OrdersProductDto;
import com.GujjuSajang.order.entity.Orders;
import com.GujjuSajang.order.entity.OrdersProduct;
import com.GujjuSajang.order.repository.OrdersProductRepository;
import com.GujjuSajang.order.repository.OrdersRepository;
import com.GujjuSajang.order.service.OrdersService;
import com.GujjuSajang.order.type.OrdersStatus;
import com.GujjuSajang.product.dto.ProductDetailDto;
import com.GujjuSajang.product.dto.ProductDto;
import com.GujjuSajang.product.dto.ProductPageDto;
import com.GujjuSajang.product.entity.Product;
import com.GujjuSajang.product.repository.ProductRepository;
import com.GujjuSajang.product.stock.Entity.Stock;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private OrdersProductRepository ordersProductRepository;
    @Mock
    private CartRedisRepository cartRedisRepository;
    @Mock
    private StockRepository stockRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private OrdersService ordersService;

    Member member;
    TokenMemberInfo tokenMemberInfo;
    Product product;
    ProductDetailDto productDetailDto;
    ProductPageDto productPageDto;
    ProductDto productDto;
    Stock stock;
    CartDto cartDto;
    CartProductsDto cartProductsDto;
    Orders orders;
    OrdersProduct ordersProduct;

    @BeforeEach
    void init() {
        member = Member.builder()
                .id(1L)
                .name("name")
                .phone("phone")
                .address("address")
                .password("encodedPassword")
                .mail("mail@test.com")
                .mailVerified(true)
                .role(MemberRole.MEMBER)
                .build();
        product = Product.builder()
                .id(1L)
                .sellerId(1L)
                .name("제품")
                .price(1000)
                .description("상세")
                .build();
        productDetailDto = ProductDetailDto.builder()
                .id(1L)
                .sellerId(1L)
                .name("제품")
                .price(1000)
                .description("상세")
                .count(0)
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
                .mail("mail@test.com")
                .mailVerified(true)
                .role(MemberRole.SELLER)
                .build();
        stock = Stock.builder()
                .id(1L)
                .productId(1L)
                .count(5)
                .build();
        cartDto = new CartDto();
        cartProductsDto = CartProductsDto.builder()
                .productID(product.getId())
                .name(product.getName())
                .count(3)
                .price(1000)
                .build();
        orders = Orders.builder()
                .id(1L)
                .memberId(1L)
                .totalPrice(3000)
                .build();
        orders.setCreateAt(LocalDateTime.now());
        ordersProduct = OrdersProduct.builder()
                .id(1L)
                .ordersId(1L)
                .productId(1L)
                .count(3)
                .price(1000)
                .status(OrdersStatus.COMPLETE)
                .build();
        ordersProduct.setCreateAt(LocalDateTime.now());
    }

    @DisplayName("주문 성공")
    @Test
    void createOrders_Success() {
        // given
        cartDto.getCartProductsDtos().add(cartProductsDto);
        given(cartRedisRepository.get(anyLong())).willReturn(Optional.ofNullable(cartDto));
        given(stockRepository.findAllByProductIdIn(any())).willReturn(List.of(stock));
        given(ordersRepository.save(any())).willReturn(orders);

        // when
        OrdersDto result = ordersService.createOrder(member.getId(), member.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orders.getId());
        assertThat(result.getTotalPrice()).isEqualTo(orders.getTotalPrice());
        verify(cartRedisRepository).delete(anyLong());
    }

    @DisplayName("주문 실패 - 장바구니 없음")
    @Test
    void createOrders_Fail_CartNotFound() {
        //given
        given(cartRedisRepository.get(anyLong())).willReturn(Optional.empty());

        //when

        //then
        assertThatThrownBy(() -> ordersService.createOrder(member.getId(), tokenMemberInfo.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_ORDER_PRODUCT.getErrorMessage());
    }

    @DisplayName("주문 실패 - 재고 부족")
    @Test
    void createOrders_Fail_NotEnoughStock() {
        //given
        cartDto.getCartProductsDtos().add(cartProductsDto);
        Stock insufficientStock = Stock.builder()
                .id(stock.getId())
                .productId(stock.getProductId())
                .count(1)
                .build();
        given(cartRedisRepository.get(anyLong())).willReturn(Optional.of(cartDto));
        given(stockRepository.findAllByProductIdIn(any())).willReturn(List.of(insufficientStock));

        //when

        // then
        assertThatThrownBy(() -> ordersService.createOrder(member.getId(), tokenMemberInfo.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.NOT_ENOUGH_STOCK.getErrorMessage());
    }

    @DisplayName("주문 취소 성공")
    @Test
    void cancelOrderProduct_Success() {
        //given
        ordersProduct.changeStatus(OrdersStatus.COMPLETE);
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));
        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));
        given(stockRepository.findByProductId(anyLong())).willReturn(Optional.of(stock));

        //when
        OrdersProductDto result = ordersService.cancelOrderProduct(member.getId(), ordersProduct.getId(), tokenMemberInfo.getId());

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ordersProduct.getId());
        assertThat(result.getOrdersId()).isEqualTo(ordersProduct.getId());
        assertThat(result.getProductId()).isEqualTo(ordersProduct.getProductId());
        assertThat(result.getName()).isEqualTo(product.getName());
        assertThat(result.getPrice()).isEqualTo(ordersProduct.getPrice());
        assertThat(result.getStatus()).isEqualTo(OrdersStatus.CANCEL);
    }

    @DisplayName("주문 취소 실패 - 취소 기간 지남")
    @Test
    void cancelOrderProduct_Fail_OverTime() {
        //given
        ordersProduct.changeStatus(OrdersStatus.COMPLETE);
        ordersProduct.setCreateAt(LocalDateTime.now().minusDays(2));
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));
        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        //when

        // then
        assertThatThrownBy(() -> ordersService.cancelOrderProduct(member.getId(), ordersProduct.getId(), tokenMemberInfo.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.ORDER_CANCELLATION_PERIOD_EXPIRED.getErrorMessage());
    }

    @DisplayName("주문 취소 실패 - 주문 상품 없음")
    @Test
    void cancelOrderProduct_Fail_OrderProductNotFound() {
        //given
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.empty());

        //when

        //then
        assertThatThrownBy(() -> ordersService.cancelOrderProduct(member.getId(), ordersProduct.getId(), tokenMemberInfo.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_ORDERED_PRODUCT.getErrorMessage());
    }

    @DisplayName("주문 취소 실패 - 제품 자체가 없음")
    @Test
    void cancelOrderProduct_Fail_ProductNotFound() {
        //given
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));
        given(productRepository.findById(anyLong())).willReturn(Optional.empty());

        //when

        //then
        assertThatThrownBy(() -> ordersService.cancelOrderProduct(member.getId(), ordersProduct.getId(), tokenMemberInfo.getId()))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_PRODUCT.getErrorMessage());
    }

    @DisplayName("주문 취소 실패 - 회원 불일치")
    @Test
    void cancelOrderProduct_Fail_MissMatchMember() {
        //given

        //when

        //then
        assertThatThrownBy(() -> ordersService.cancelOrderProduct(member.getId(), ordersProduct.getId(), tokenMemberInfo.getId() + 1))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(ErrorCode.MISS_MATCH_MEMBER.getErrorMessage());
    }

    @DisplayName("주문 반품 성공")
    @Test
    void returnOrderProduct_Success() {
        //given
        ordersProduct.changeStatus(OrdersStatus.COMPLETED_DELIVERY);
        ordersProduct.setUpdateAt(LocalDateTime.now());
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));
        given(productRepository.findById(anyLong())).willReturn(Optional.of(product));

        //when
        OrdersProductDto result = ordersService.returnOrderProduct(member.getId(), ordersProduct.getId(), tokenMemberInfo.getId());

        //then
        assertThat(result).isNotNull();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ordersProduct.getId());
        assertThat(result.getOrdersId()).isEqualTo(ordersProduct.getId());
        assertThat(result.getProductId()).isEqualTo(ordersProduct.getProductId());
        assertThat(result.getName()).isEqualTo(product.getName());
        assertThat(result.getPrice()).isEqualTo(ordersProduct.getPrice());
        assertThat(result.getStatus()).isEqualTo(OrdersStatus.RETURN_REQUEST);
    }

    @DisplayName("주문 반품 실패 - 반품을 요청할 수 있는 상태가 아님")
    @Test
    void returnOrderProduct_Fail_NotInReturnRequestStatus() {
        // given
        ordersProduct.changeStatus(OrdersStatus.COMPLETE);
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));

        // when

        // then
        assertThatThrownBy(() -> ordersService.returnOrderProduct(member.getId(), ordersProduct.getId(), tokenMemberInfo.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.ACTION_NOT_ALLOWED.getErrorMessage());
    }

    @DisplayName("주문 반품 실패 - 제품이 존재하지 않음")
    @Test
    void returnOrderProduct_Fail_ProductNotFound() {
        // given
        ordersProduct.changeStatus(OrdersStatus.COMPLETED_DELIVERY);
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));
        given(productRepository.findById(anyLong())).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> ordersService.returnOrderProduct(member.getId(), ordersProduct.getId(), tokenMemberInfo.getId()))
                .isInstanceOf(ProductException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_PRODUCT.getErrorMessage());
    }

    @DisplayName("주문 반품 실패 - 회원 불일치")
    @Test
    void returnOrderProduct_Fail_MissMatchMember() {
        //given

        //when

        //then
        assertThatThrownBy(() -> ordersService.returnOrderProduct(1L, ordersProduct.getId(), tokenMemberInfo.getId() + 1))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(ErrorCode.MISS_MATCH_MEMBER.getErrorMessage());
    }

    @DisplayName("주문 조회 성공")
    @Test
    void getOrder_Success() {
        // given
        Page<Orders> ordersPage = new PageImpl<>(List.of(orders), PageRequest.of(0, 10), 1);
        given(ordersRepository.findByMemberIdOrderByCreateAtDesc(anyLong(), any(Pageable.class))).willReturn(ordersPage);

        // when
        OrdersPageDto result = ordersService.getOrder(member.getId(), tokenMemberInfo.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrdersDtos()).hasSize(1);
        assertThat(result.getOrdersDtos().get(0).getId()).isEqualTo(orders.getId());
        assertThat(result.getOrdersDtos().get(0).getTotalPrice()).isEqualTo(orders.getTotalPrice());
    }

    @DisplayName("주문 조회 실패 - 회원 불일치")
    @Test
    void getOrder_Fail_InvalidMemberId() {
        // given

        // when

        // then
        assertThatThrownBy(() -> ordersService.getOrder(member.getId(), tokenMemberInfo.getId() + 1, PageRequest.of(0, 10)))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(ErrorCode.MISS_MATCH_MEMBER.getErrorMessage());
    }

    @DisplayName("주문 내역 상세 조회 성공")
    @Test
    void getOrderProducts_Success() {
        // given
        ordersProduct.setCreateAt(LocalDateTime.now());
        ordersProduct.setUpdateAt(LocalDateTime.now());
        List<OrdersProduct> ordersProducts = List.of(ordersProduct);
        given(ordersProductRepository.findByOrdersId(anyLong())).willReturn(ordersProducts);
        given(productRepository.findAllById(anyList())).willReturn(List.of(product));

        // when
        List<OrdersProductDto> result = ordersService.getOrderProducts(orders.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(ordersProduct.getId());
        assertThat(result.get(0).getProductId()).isEqualTo(ordersProduct.getProductId());
        assertThat(result.get(0).getOrdersId()).isEqualTo(ordersProduct.getOrdersId());
        assertThat(result.get(0).getCount()).isEqualTo(ordersProduct.getCount());
        assertThat(result.get(0).getName()).isEqualTo(product.getName());
        assertThat(result.get(0).getPrice()).isEqualTo(ordersProduct.getPrice());
        assertThat(result.get(0).getStatus()).isEqualTo(ordersProduct.getStatus());
    }

    @DisplayName("주문 내역 상세 조회 실패 - 주문 내역 없음")
    @Test
    void getOrderProducts_Fail_OrderNotFound() {
        // given
        given(ordersProductRepository.findByOrdersId(anyLong())).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> ordersService.getOrderProducts(orders.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_ORDER_PRODUCT.getErrorMessage());
    }

}