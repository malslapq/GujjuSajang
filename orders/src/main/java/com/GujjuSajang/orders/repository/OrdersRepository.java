package com.GujjuSajang.orders.repository;

import com.GujjuSajang.orders.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
