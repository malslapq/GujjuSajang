package com.GujjuSajang.order.repository;

import com.GujjuSajang.order.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findByMemberIdOrderByCreateAtDesc(Long memberId, Pageable pageable);
}
