package com.GujjuSajang.orders.event;

import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CreateOrderEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrdersEventProducer {

    private final EventProducer eventProducer;

    // 주문 요청 이벤트
    @Transactional
    public void createOrder(Long memberId, CartDto cartDto) {

        eventProducer.sendEvent(
                "create-orders",
                CreateOrderEventDto.builder()
                        .memberId(memberId)
                        .cartProductsDtos(cartDto.getCartProductsDtos())
                        .build());

    }


}
