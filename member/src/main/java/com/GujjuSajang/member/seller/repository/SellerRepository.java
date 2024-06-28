package com.GujjuSajang.member.seller.repository;

import com.GujjuSajang.member.seller.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
}
