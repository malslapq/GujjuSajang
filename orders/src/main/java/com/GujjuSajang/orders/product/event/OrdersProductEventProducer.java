package com.GujjuSajang.orders.product.event;

import com.GujjuSajang.core.dto.UpdateOrdersProductStatusDto;
import com.GujjuSajang.core.dto.UpdateStockDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.OrdersException;
import com.GujjuSajang.core.type.DeliveryStatus;
import com.GujjuSajang.orders.product.dto.OrdersProductDto;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.product.entity.OrdersProduct;
import com.GujjuSajang.orders.event.EventProducer;
import com.GujjuSajang.orders.repository.OrdersProductRepository;
import com.GujjuSajang.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrdersProductEventProducer {

    private final OrdersRepository ordersRepository;
    private final OrdersProductRepository ordersProductRepository;
    private final EventProducer eventProducer;

    // 주문 제품 취소
    @Transactional
    public OrdersProductDto cancelOrderProduct(Long memberId, Long orderProductId) {

        OrdersProduct ordersProduct = getOrderProduct(orderProductId);
        Orders orders = ordersRepository.findById(ordersProduct.getOrdersId()).orElseThrow(() -> new OrdersException(ErrorCode.NOT_FOUND_ORDER_PRODUCT));
        validateOrdersMemberId(memberId, orders.getMemberId());
        validateOrderProductStatus(ordersProduct.getStatus());
        ordersProduct.changeStatus(DeliveryStatus.CANCEL);
        validateOrderPeriods(ordersProduct.getCreatedAt().plusDays(1));

        eventProducer.sendEvent("stock-update",
                UpdateStockDto.builder()
                        .productId(ordersProduct.getProductId())
                        .count(ordersProduct.getCount())
                        .build());

        return OrdersProductDto.from(ordersProduct);
    }

    @Transactional
    @Scheduled(cron = "0 0 * * * *")
    public void updateStatus() {
        List<DeliveryStatus> statuses = Arrays.asList(DeliveryStatus.COMPLETE, DeliveryStatus.DELIVERY, DeliveryStatus.RETURN_REQUEST);
        List<OrdersProduct> ordersProductList = ordersProductRepository.findByStatusIn(statuses);
        Map<Long, Integer> ordersProductCountMap = new HashMap<>();
        List<Long> ordersProductIds = new ArrayList<>();

        for (OrdersProduct ordersProduct : ordersProductList) {
            Long ids = ordersProduct.updateDeliveryStatus();
            if (ids != -1) {
                ordersProductCountMap.put(ids, ordersProduct.getCount());
                ordersProductIds.add(ordersProduct.getId());
            }
        }

        if (!ordersProductCountMap.isEmpty()) {
            List<Long> productIds = ordersProductCountMap.keySet().stream().toList();
            eventProducer.sendEvent("return-completed-ordersProduct",
                    UpdateOrdersProductStatusDto.builder()
                            .ordersProductIds(ordersProductIds)
                            .productIds(productIds)
                            .ordersProductCountsMap(ordersProductCountMap)
                            .build());
        }

    }

    private static void validateOrderPeriods(LocalDateTime orderCreateAtPlusDays) {
        if (LocalDateTime.now().isAfter(orderCreateAtPlusDays)) {
            throw new OrdersException(ErrorCode.ORDER_CANCELLATION_PERIOD_EXPIRED);
        }
    }

    private static void validateOrderProductStatus(DeliveryStatus status) {
        if (status != DeliveryStatus.COMPLETE) {
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
