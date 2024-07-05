package com.GujjuSajang.cart.event;

import com.GujjuSajang.cart.repository.CartRedisRepository;
import com.GujjuSajang.core.dto.CartDto;
import com.GujjuSajang.core.dto.CreateMemberEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartEventConsumer {

    private final CartRedisRepository cartRedisRepository;
    private final EventProducer eventProducer;

    @Transactional
    @KafkaListener(topics = {"create-member"}, groupId = "createCart")
    public void createCart(CreateMemberEventDto createMemberEventDto) {
        try {
            cartRedisRepository.save(createMemberEventDto.getId(), new CartDto());
        } catch (Exception e) {
            eventProducer.sendEvent("fail-create-cart", createMemberEventDto);
        }
    }
}
