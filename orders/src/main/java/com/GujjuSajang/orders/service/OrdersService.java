package com.GujjuSajang.orders.service;

import com.GujjuSajang.core.dto.*;
import com.GujjuSajang.core.entity.OrdersProduct;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.OrdersException;
import com.GujjuSajang.core.service.EventProducerService;
import com.GujjuSajang.core.type.OrdersStatus;
import com.GujjuSajang.orders.dto.OrdersDto;
import com.GujjuSajang.orders.dto.OrdersPageDto;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.repository.OrdersProductRepository;
import com.GujjuSajang.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrdersProductRepository ordersProductRepository;
    private final EventProducerService eventProducerService;

    // 주문 생성
    @Transactional
    public OrdersDto createOrder(Long memberId, CartDto cartDto) {

        Orders orders = ordersRepository.save(Orders.of(memberId, cartDto));

        eventProducerService.sendEventWithKey(
                "create-orders",
                "orders",
                CreateOrderEventDto.builder()
                        .cartProductsDtos(cartDto.getCartProductsDtos())
                        .orderId(orders.getId())
                        .build());

        return OrdersDto.from(orders);
    }

    // 주문 조회
    @Transactional(readOnly = true)
    public OrdersPageDto getOrder(Long memberId, Pageable pageable) {
        Page<Orders> orders = ordersRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        List<OrdersDto> ordersDtos = orders.getContent().stream().map(OrdersDto::from).toList();
        return OrdersPageDto.builder()
                .ordersDtos(ordersDtos)
                .pageNumber(orders.getNumber())
                .pageSize(orders.getSize())
                .totalCount(orders.getTotalElements())
                .totalPage(orders.getTotalPages())
                .last(orders.isLast())
                .build();
    }

    // 주문 내역 상세 조회
    @Transactional
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

    // 주문 제품 취소
    @Transactional
    public OrdersProductDto cancelOrderProduct(Long memberId, Long orderProductId) {

        OrdersProduct ordersProduct = getOrderProduct(orderProductId);
        Orders orders = ordersRepository.findById(ordersProduct.getOrdersId()).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDER_PRODUCT));
        validateOrdersMemberId(memberId, orders.getMemberId());
        validateOrderProductStatus(ordersProduct.getStatus(), OrdersStatus.COMPLETE);
        ordersProduct.changeStatus(OrdersStatus.CANCEL);
        validateOrderPeriods(ordersProduct.getCreatedAt().plusDays(1));

        eventProducerService.sendEvent("stock-update",
                UpdateStockDto.builder()
                        .productId(ordersProduct.getProductId())
                        .count(ordersProduct.getCount())
                        .build());

        return OrdersProductDto.from(ordersProduct);
    }

    // 주문 제품 반품 신청
    @Transactional
    public OrdersProductDto returnOrderProduct(Long memberId, Long orderProductId) {
        OrdersProduct ordersProduct = getOrderProduct(orderProductId);
        Orders orders = ordersRepository.findById(ordersProduct.getOrdersId()).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERS));
        validateOrdersMemberId(memberId, orders.getMemberId());
        validateOrderProductStatus(ordersProduct.getStatus(), OrdersStatus.COMPLETED_DELIVERY);
        validateOrderPeriods(ordersProduct.getUpdateAt().plusDays(1));
        ordersProduct.changeStatus(OrdersStatus.RETURN_REQUEST);
        return OrdersProductDto.from(ordersProduct);
    }

    private static void validateOrderPeriods(LocalDateTime orderCreateAtPlusDays) {
        if (LocalDateTime.now().isAfter(orderCreateAtPlusDays)) {
            throw new OrdersException(ErrorCode.ORDER_CANCELLATION_PERIOD_EXPIRED);
        }
    }

    private static void validateOrderProductStatus(OrdersStatus orderProductStatus, OrdersStatus status) {
        if (orderProductStatus != status) {
            throw new OrdersException(ErrorCode.ACTION_NOT_ALLOWED);
        }
    }

    private static void validateOrdersMemberId(Long memberId, Long ordersMemberId) {
        if (!memberId.equals(ordersMemberId)) {
            throw new OrdersException(ErrorCode.ORDER_NOT_BELONG_TO_MEMBER);
        }
    }

    private OrdersProduct getOrderProduct(Long orderProductId) {
        return ordersProductRepository.findById(orderProductId).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDERED_PRODUCT));
    }
}
