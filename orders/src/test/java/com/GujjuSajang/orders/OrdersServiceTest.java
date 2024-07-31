package com.GujjuSajang.orders;

import com.GujjuSajang.core.type.OrdersStatus;
import com.GujjuSajang.orders.dto.OrdersPageDto;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.repository.OrdersRepository;
import com.GujjuSajang.orders.service.OrdersService;
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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrdersServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @InjectMocks
    private OrdersService ordersService;

    private Orders orders;


    @BeforeEach
    void setUp() {
        orders = Orders.builder()
                .id(1L)
                .memberId(1L)
                .paymentId(1L)
                .totalPrice(10000)
                .status(OrdersStatus.COMPLETE)
                .build();
    }

    @DisplayName("주문 조회 - 성공")
    @Test
    void getOrder_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Orders> ordersList = Collections.singletonList(orders);
        Page<Orders> ordersPage = new PageImpl<>(ordersList, pageable, ordersList.size());
        given(ordersRepository.findByMemberIdOrderByCreatedAtDesc(any(Long.class), any(Pageable.class)))
                .willReturn(ordersPage);

        // when
        OrdersPageDto result = ordersService.getOrder(1L, pageable);

        // then
        assertThat(result.getOrdersDtos()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getTotalPage()).isEqualTo(1);
        assertThat(result.getOrdersDtos().get(0).getId()).isEqualTo(orders.getId());
        assertThat(result.getOrdersDtos().get(0).getTotalPrice()).isEqualTo(orders.getTotalPrice());
        assertThat(result.getOrdersDtos().get(0).getStatus()).isEqualTo(orders.getStatus());
        verify(ordersRepository).findByMemberIdOrderByCreatedAtDesc(any(Long.class), any(Pageable.class));
    }

    @DisplayName("주문 조회 - 주문 없음")
    @Test
    void getOrder_NotFoundOrders() {
        // given
        Long memberId = 2L; // 주문이 없는 회원 ID
        Pageable pageable = PageRequest.of(0, 10);
        Page<Orders> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        given(ordersRepository.findByMemberIdOrderByCreatedAtDesc(any(Long.class), any(Pageable.class)))
                .willReturn(emptyPage);

        // when
        OrdersPageDto result = ordersService.getOrder(memberId, pageable);

        // then
        assertThat(result.getOrdersDtos()).isEmpty();
        assertThat(result.getTotalCount()).isEqualTo(0);
        verify(ordersRepository).findByMemberIdOrderByCreatedAtDesc(any(Long.class), any(Pageable.class));
    }

}