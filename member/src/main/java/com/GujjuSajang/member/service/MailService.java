package com.GujjuSajang.member.service;

import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.member.repository.MailVerifiedRedisRepository;
import com.GujjuSajang.member.util.mail.MailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailSender mailSender;
    private final MailVerifiedRedisRepository mailVerifiedRedisRepository;

    @Transactional
    @KafkaListener(topics = {"create-member"}, groupId = "sendMail")
    public void sendMail(CreateMemberEventDto createMemberEventDto) {
        mailSender.sendVerifiedMail(createMemberEventDto.getId(), createMemberEventDto.getMail(), createMemberEventDto.getCode());
    }

    @Transactional
    @KafkaListener(topics = {"create-member"}, groupId = "saveCode")
    public void saveVerifiedMail(CreateMemberEventDto createMemberEventDto) {
        mailVerifiedRedisRepository.save(createMemberEventDto.getId(), createMemberEventDto.getCode());
    }

}
