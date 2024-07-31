package com.GujjuSajang.orders;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.OrdersException;
import com.GujjuSajang.core.type.DeliveryStatus;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.product.dto.OrdersProductDto;
import com.GujjuSajang.orders.product.entity.OrdersProduct;
import com.GujjuSajang.orders.product.service.OrdersProductService;
import com.GujjuSajang.orders.repository.OrdersProductRepository;
import com.GujjuSajang.orders.repository.OrdersRepository;
import com.GujjuSajang.orders.service.OrdersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrdersProductServiceTest {

    @Mock
    private OrdersRepository ordersRepository;
    @Mock
    private OrdersProductRepository ordersProductRepository;
    @Mock
    private OrdersService ordersService;
    @InjectMocks
    private OrdersProductService ordersProductService;

    Orders orders;
    OrdersProduct ordersProduct;

    @BeforeEach
    void init() {
        orders = Orders.builder()
                .id(1L)
                .memberId(1L)
                .totalPrice(9000)
                .build();
        orders.setCreatedAt(LocalDateTime.now());
        ordersProduct = OrdersProduct.builder()
                .id(1L)
                .ordersId(1L)
                .productId(1L)
                .count(3)
                .price(1000)
                .status(DeliveryStatus.COMPLETE)
                .build();
        ordersProduct.setCreatedAt(LocalDateTime.now());
        ordersProduct.setUpdateAt(LocalDateTime.now());
    }

    @DisplayName("주문 상세 내역 조회 - 성공")
    @Test
    void getOrdersProducts_Success() {
        //given

        //when

        //then

    }

    @DisplayName("주문 상세 내역 조회 실패 - 주문 없음")
    @Test
    void getOrdersProducts_Fail_NotFoundOrders() {
        //given

        //when

        //then

    }

    @DisplayName("주문 상세 내역 조회 실패 - 주문 상세 내역 없음")
    @Test
    void getOrdersProducts_Fail_NotFoundOrdersProducts() {
        //given

        //when

        //then

    }

    @DisplayName("주문 제품 반품 - 성공")
    @Test
    void removeOrdersProducts_Success() {
        //given

        //when

        //then

    }

    @DisplayName("주문 반품 성공")
    @Test
    void returnOrderProduct_Success() {
        //given
        ordersProduct.changeStatus(DeliveryStatus.COMPLETED_DELIVERY);
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));
        given(ordersRepository.findById(anyLong())).willReturn(Optional.of(orders));


        //when
        OrdersProductDto result = ordersProductService.returnOrderProduct(1L, ordersProduct.getId());

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ordersProduct.getId());
        assertThat(result.getOrdersId()).isEqualTo(ordersProduct.getId());
        assertThat(result.getProductId()).isEqualTo(ordersProduct.getProductId());
        assertThat(result.getName()).isEqualTo(ordersProduct.getName());
        assertThat(result.getPrice()).isEqualTo(ordersProduct.getPrice());
        assertThat(result.getStatus()).isEqualTo(DeliveryStatus.RETURN_REQUEST);
    }

    @DisplayName("주문 반품 실패 - 반품을 요청할 수 있는 상태가 아님")
    @Test
    void returnOrderProduct_Fail_NotInReturnRequestStatus() {
        // given
        ordersProduct.changeStatus(DeliveryStatus.COMPLETE);
        given(ordersRepository.findById(anyLong())).willReturn(Optional.of(orders));
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));

        // when

        // then
        assertThatThrownBy(() -> ordersProductService.returnOrderProduct(1L, ordersProduct.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.ACTION_NOT_ALLOWED.getErrorMessage());
    }

    @DisplayName("주문 반품 실패 - 제품이 존재하지 않음")
    @Test
    void returnOrderProduct_Fail_ProductNotFound() {
        // given
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> ordersProductService.returnOrderProduct(1L, ordersProduct.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.NOT_FOUND_ORDERED_PRODUCT.getErrorMessage());
    }

    @DisplayName("주문 반품 실패 - 회원 불일치")
    @Test
    void returnOrderProduct_Fail_MissMatchMember() {
        //given
        orders = Orders.builder()
                .id(1L)
                .memberId(2L) // 다른 회원 ID로 설정
                .totalPrice(9000)
                .build();
        ordersProduct.changeStatus(DeliveryStatus.COMPLETED_DELIVERY);
        given(ordersProductRepository.findById(anyLong())).willReturn(Optional.of(ordersProduct));
        given(ordersRepository.findById(anyLong())).willReturn(Optional.of(orders));

        //when

        //then
        assertThatThrownBy(() -> ordersProductService.returnOrderProduct(1L, ordersProduct.getId()))
                .isInstanceOf(OrdersException.class)
                .hasMessageContaining(ErrorCode.ORDER_NOT_BELONG_TO_MEMBER.getErrorMessage());
    }


}
