package com.GujjuSajang.Consumer.service;

import com.GujjuSajang.Consumer.dto.ConsumerDetailDto;
import com.GujjuSajang.Consumer.dto.ConsumerSignUpDto;
import com.GujjuSajang.Consumer.entity.Consumer;
import com.GujjuSajang.Consumer.repository.ConsumerRepository;
import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenUserInfo;
import com.GujjuSajang.Jwt.service.JwtService;
import com.GujjuSajang.exception.ConsumerException;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.redis.repository.MailVerifiedRepository;
import com.GujjuSajang.util.Mail.MailSender;
import com.GujjuSajang.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;
    private final MailVerifiedRepository mailVerifiedRepository;

    // 회원 가입
    @Transactional
    public TokenInfo signUp(ConsumerSignUpDto consumerSignUpDto) {

        String encodedPassword = passwordEncoder.encode(consumerSignUpDto.getPassword());

        Consumer consumer = consumerRepository.save(
                Consumer.builder()
                        .name(consumerSignUpDto.getName())
                        .password(encodedPassword)
                        .mail(consumerSignUpDto.getMail())
                        .mailVerified(false)
                        .phone(consumerSignUpDto.getPhone())
                        .address(consumerSignUpDto.getAddress())
                        .build()
        );

        mailVerifiedRepository.save(consumer.getId(), mailSender.sendVerifiedMail(consumer.getId(), consumer.getMail()));

        return jwtService.issueTokens(TokenUserInfo.builder()
                .id(consumer.getId())
                .mail(consumer.getMail())
                .build()
        );
    }

    @Transactional
    public void verifiedMail(long id, String code) {
        Consumer consumer = consumerRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        String getCode = mailVerifiedRepository.getCode(id).orElseThrow(() -> new ConsumerException(ErrorCode.INVALID_CODE));
        if (code.equals(getCode)) {
            consumer.changeMailVerified(true);
        }
    }


}
