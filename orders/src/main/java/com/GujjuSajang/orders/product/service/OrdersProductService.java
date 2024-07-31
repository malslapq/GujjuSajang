package com.GujjuSajang.orders.product.service;


import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.OrdersException;
import com.GujjuSajang.core.type.DeliveryStatus;
import com.GujjuSajang.orders.product.dto.OrdersProductDto;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.product.entity.OrdersProduct;
import com.GujjuSajang.orders.repository.OrdersProductRepository;
import com.GujjuSajang.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersProductService {

    private final OrdersRepository ordersRepository;
    private final OrdersProductRepository ordersProductRepository;

    // 주문 내역 상세 조회
    @Transactional(readOnly = true)
    public List<OrdersProductDto> getOrderProducts(Long memberId, Long orderId) {

        Orders orders = ordersRepository.findById(orderId).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERS));

        validateOrdersMemberId(memberId, orders.getMemberId());

        List<OrdersProduct> ordersProducts = ordersProductRepository.findByOrdersId(orderId);

        if (ordersProducts.isEmpty()) {
            throw new OrdersException(ErrorCode.NOT_FOUND_ORDER_PRODUCT);
        }

        return ordersProducts.stream()
                .map(OrdersProductDto::from)
                .toList();
    }

    // 주문 제품 반품 신청
    @Transactional
    public OrdersProductDto returnOrderProduct(Long memberId, Long orderProductId) {
        OrdersProduct ordersProduct = getOrderProduct(orderProductId);
        Orders orders = ordersRepository.findById(ordersProduct.getOrdersId()).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERS));
        validateOrdersMemberId(memberId, orders.getMemberId());
        validateOrderProductStatus(ordersProduct.getStatus());
        validateOrderPeriods(ordersProduct.getUpdateAt().plusDays(1));
        ordersProduct.changeStatus(DeliveryStatus.RETURN_REQUEST);
        return OrdersProductDto.from(ordersProduct);
    }


    private static void validateOrdersMemberId(Long memberId, Long ordersMemberId) {
        if (!memberId.equals(ordersMemberId)) {
            throw new OrdersException(ErrorCode.ORDER_NOT_BELONG_TO_MEMBER);
        }
    }

    private static void validateOrderPeriods(LocalDateTime orderCreateAtPlusDays) {
        if (LocalDateTime.now().isAfter(orderCreateAtPlusDays)) {
            throw new OrdersException(ErrorCode.ORDER_CANCELLATION_PERIOD_EXPIRED);
        }
    }

    private static void validateOrderProductStatus(DeliveryStatus deliveryStatus) {
        if (deliveryStatus != DeliveryStatus.COMPLETED_DELIVERY) {
            throw new OrdersException(ErrorCode.ACTION_NOT_ALLOWED);
        }
    }

    private OrdersProduct getOrderProduct(Long orderProductId) {
        return ordersProductRepository.findById(orderProductId).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERED_PRODUCT));
    }

}
