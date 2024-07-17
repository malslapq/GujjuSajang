package com.GujjuSajang.orders.repository;

import com.GujjuSajang.core.type.OrdersStatus;
import com.GujjuSajang.orders.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Orders o SET o.status = :status WHERE o.id = :orderId")
    void setStatus(@Param("orderId") Long orderId, @Param("status") OrdersStatus status);
}
