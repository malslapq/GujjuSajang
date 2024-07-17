package com.GujjuSajang.orders.service;

import com.GujjuSajang.orders.dto.OrdersDto;
import com.GujjuSajang.orders.dto.OrdersPageDto;
import com.GujjuSajang.orders.entity.Orders;
import com.GujjuSajang.orders.repository.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;

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

}
