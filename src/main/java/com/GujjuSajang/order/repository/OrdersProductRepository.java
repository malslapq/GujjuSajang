package com.GujjuSajang.order.repository;

import com.GujjuSajang.order.entity.OrdersProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersProductRepository extends JpaRepository<OrdersProduct, Long> {
    List<OrdersProduct> findByOrdersId(Long orderId);
}
