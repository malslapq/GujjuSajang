package com.GujjuSajang.member.event;

import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.member.repository.MailVerifiedRedisRepository;
import com.GujjuSajang.member.util.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailEventConsumer {

    private final MailSender mailSender;
    private final MailVerifiedRedisRepository mailVerifiedRedisRepository;
    private final EventProducer eventProducer;

    @Transactional
    @KafkaListener(topics = {"create-member"}, groupId = "sendMail")
    public void sendMail(CreateMemberEventDto createMemberEventDto) {
        try {
            mailSender.sendVerifiedMail(createMemberEventDto.getId(), createMemberEventDto.getMail(), createMemberEventDto.getCode());
        } catch (Exception e) {
            eventProducer.sendEvent("fail-send-mail", createMemberEventDto);
        }
    }

    @Transactional
    @KafkaListener(topics = {"create-member"}, groupId = "saveCode")
    public void saveVerifiedMail(CreateMemberEventDto createMemberEventDto) {
        try {
            mailVerifiedRedisRepository.save(createMemberEventDto.getId(), createMemberEventDto.getCode());
        } catch (Exception e) {
            eventProducer.sendEvent("fail-save-code", createMemberEventDto);
        }

    }

}
