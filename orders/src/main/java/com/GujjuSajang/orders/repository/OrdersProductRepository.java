package com.GujjuSajang.orders.repository;

import com.GujjuSajang.core.entity.OrdersProduct;
import com.GujjuSajang.core.type.OrdersStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersProductRepository extends JpaRepository<OrdersProduct, Long> {
    List<OrdersProduct> findByOrdersId(Long orderId);
    List<OrdersProduct> findByStatusIn(List<OrdersStatus> statuses);
}
