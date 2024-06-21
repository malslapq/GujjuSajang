package com.GujjuSajang.Consumer.repository;

import com.GujjuSajang.Consumer.entity.Consumer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConsumerRepository extends JpaRepository<Consumer, Long> {
    Optional<Consumer> findByMail(String mail);
}
