package com.GujjuSajang.member.event;

import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MemberEventConsumer.class);
    private final MemberRepository memberRepository;

    @KafkaListener(topics = {"fail-send-mail", "fail-save-code", "fail-create-cart"})
    @Transactional
    public void compensationCreateMember(CreateMemberEventDto createMemberEventDto) {
        try {
            if (memberRepository.existsById(createMemberEventDto.getId())) {
                memberRepository.deleteById(createMemberEventDto.getId());
            }
        } catch (Exception e) {
            logger.error("Failed to compensate for member with ID: {}", createMemberEventDto.getId(), e);
        }
    }
}
