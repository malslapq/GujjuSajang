package com.GujjuSajang.orders.event;

import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CartProductsDto;
import com.GujjuSajang.core.dto.CreateFirstComeOrdersEventDto;
import com.GujjuSajang.core.dto.CreateOrderEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrdersEventProducer {

    private final EventProducer eventProducer;

    // 주문 요청 이벤트
    public void createOrder(Long memberId, CartDto cartDto) {

        eventProducer.sendEvent(
                "request-orders",
                CreateOrderEventDto.builder()
                        .memberId(memberId)
                        .cartProductsDtos(cartDto.getCartProductsDtos())
                        .build());

    }


    public void createFirstComeOrders(Long memberId, CartProductsDto cartProductsDto) {

        eventProducer.sendEvent(
                "create-first-come-orders",
                CreateFirstComeOrdersEventDto.builder()
                        .memberId(memberId)
                        .cartProductsDto(cartProductsDto)
                        .build());
    }
}
