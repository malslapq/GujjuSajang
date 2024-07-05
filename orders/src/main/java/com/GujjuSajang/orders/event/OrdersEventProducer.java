package com.GujjuSajang.orders.event;

import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.orders.dto.OrdersDto;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdersEventProducer {

    private final OrdersRepository ordersRepository;
    private final EventProducer eventProducer;

    // 주문 생성
    @Transactional
    public OrdersDto createOrder(Long memberId, CartDto cartDto) {

        Orders orders = ordersRepository.save(Orders.of(memberId, cartDto));

        eventProducer.sendEventWithKey(
                "create-orders",
                "orders",
                CreateOrderEventDto.builder()
                        .cartProductsDtos(cartDto.getCartProductsDtos())
                        .orderId(orders.getId())
                        .build());

        return OrdersDto.from(orders);
    }


}
