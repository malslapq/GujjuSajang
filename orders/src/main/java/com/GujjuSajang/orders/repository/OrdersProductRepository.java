package com.GujjuSajang.orders.repository;

import com.GujjuSajang.core.type.DeliveryStatus;
import com.GujjuSajang.orders.product.entity.OrdersProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OrdersProductRepository extends JpaRepository<OrdersProduct, Long> {
    List<OrdersProduct> findByOrdersId(Long orderId);
    List<OrdersProduct> findByStatusIn(List<DeliveryStatus> statuses);

    @Modifying
    @Transactional
    @Query("UPDATE OrdersProduct o SET o.status = :status WHERE o.ordersId = :ordersId")
    void updateStatusByOrdersId(@Param("ordersId") Long ordersId, @Param("status") DeliveryStatus status);
}
