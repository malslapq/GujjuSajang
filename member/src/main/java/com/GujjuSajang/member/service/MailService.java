package com.GujjuSajang.member.service;

import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.member.repository.MailVerifiedRedisRepository;
import com.GujjuSajang.member.util.mail.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailSender mailSender;
    private final MailVerifiedRedisRepository mailVerifiedRedisRepository;

    @KafkaListener(topics = {"create-member"}, groupId = "sendMail")
    public void sendMail(CreateMemberEventDto createMemberEventDto) {
        mailSender.sendVerifiedMail(createMemberEventDto.getId(), createMemberEventDto.getMail(), createMemberEventDto.getCode());
    }

    @KafkaListener(topics = {"create-member"}, groupId = "saveCode")
    public void saveVerifiedMail(CreateMemberEventDto createMemberEventDto) {
        mailVerifiedRedisRepository.save(createMemberEventDto.getId(), createMemberEventDto.getMail());
    }

}
