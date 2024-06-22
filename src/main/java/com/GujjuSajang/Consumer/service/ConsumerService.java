package com.GujjuSajang.Consumer.service;

import com.GujjuSajang.Consumer.dto.ConsumerLoginDto;
import com.GujjuSajang.Consumer.dto.ConsumerSignUpDto;
import com.GujjuSajang.Consumer.dto.ConsumerUpdateDetailDto;
import com.GujjuSajang.Consumer.dto.ConsumerUpdatePasswordDto;
import com.GujjuSajang.Consumer.entity.Consumer;
import com.GujjuSajang.Consumer.repository.ConsumerRepository;
import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenUserInfo;
import com.GujjuSajang.Jwt.service.JwtService;
import com.GujjuSajang.exception.ConsumerException;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.redis.repository.MailVerifiedRepository;
import com.GujjuSajang.redis.repository.RefreshTokenRepository;
import com.GujjuSajang.util.Mail.MailSender;
import com.GujjuSajang.util.PasswordEncoder;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConsumerService {

    private final ConsumerRepository consumerRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;
    private final MailVerifiedRepository mailVerifiedRepository;
    private final RefreshTokenRepository refreshTokenRepository;

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

        try {
            mailVerifiedRepository.save(consumer.getId(), mailSender.sendVerifiedMail(consumer.getId(), consumer.getMail()));
        } catch (MessagingException e) {
            throw new ConsumerException(ErrorCode.FAIL_SEND_MAIL);
        }

        return jwtService.issueTokens(TokenUserInfo.builder()
                .id(consumer.getId())
                .mail(consumer.getMail())
                .mailVerified(consumer.isMailVerified())
                .build()
        );
    }

    // 로그인
    public TokenInfo login(ConsumerLoginDto consumerLoginDto) {
        Consumer consumer = consumerRepository.findByMail(consumerLoginDto.getMail()).orElseThrow(() ->
                new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        passwordEncoder.matches(consumerLoginDto.getPassword(), consumer.getPassword());
        matchPassword(consumerLoginDto.getPassword(), consumer.getPassword());
        return jwtService.issueTokens(TokenUserInfo.builder()
                .id(consumer.getId())
                .mail(consumer.getMail())
                .mailVerified(consumer.isMailVerified())
                .build()
        );
    }

    // 로그아웃
    public void logout(Long id) {
        refreshTokenRepository.delete(id);
    }

    // 메일 인증
    @Transactional
    public void verifiedMail(long id, String code) {
        Consumer consumer = consumerRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        String getCode = mailVerifiedRepository.getCode(id).orElseThrow(() -> new ConsumerException(ErrorCode.INVALID_CODE));
        if (code.equals(getCode)) {
            consumer.changeMailVerified(true);
            mailVerifiedRepository.delete(id);
        }
    }

    // 구매자 조회
    @Transactional(readOnly = true)
    public ConsumerUpdateDetailDto getDetail(long id) {
        Consumer consumer = consumerRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        return Consumer.from(consumer);
    }

    // 구매자 정보 수정
    @Transactional
    public ConsumerUpdateDetailDto updateConsumer(Long id, Long tokenId, ConsumerUpdateDetailDto consumerUpdateDetailDto) {
        verifyId(tokenId, id);
        Consumer consumer = consumerRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        matchPassword(consumerUpdateDetailDto.getPassword(), consumer.getPassword());
        consumer.changeAddressAndPhone(consumerUpdateDetailDto.getAddress(), consumerUpdateDetailDto.getPhone());
        return Consumer.from(consumer);
    }

    // 비밀번호 변경
    @Transactional
    public ConsumerUpdatePasswordDto.Response updatePassword(Long id, Long tokenId, ConsumerUpdatePasswordDto consumerUpdatePasswordDto) {
        verifyId(id, tokenId);
        Consumer consumer = consumerRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        matchPassword(consumerUpdatePasswordDto.getCurPassword(), consumer.getPassword());
        consumer.changePassword(passwordEncoder.encode(consumerUpdatePasswordDto.getNewPassword()));
        return ConsumerUpdatePasswordDto.Response.builder()
                .id(consumer.getId())
                .name(consumer.getName())
                .mail(consumer.getMail())
                .phone(consumer.getPhone())
                .address(consumer.getAddress())
                .build();
    }

    // id 검증
    private void verifyId(Long tokenId, Long requestId) {
        if (!Objects.equals(tokenId, requestId)) {
            throw new ConsumerException(ErrorCode.MISS_MATCH_CONSUMER);
        }
    }

    // 비밀번호 검증
    private void matchPassword(String requestPassword, String encodedPassword) {
        if (!passwordEncoder.matches(requestPassword, encodedPassword)) {
            throw new ConsumerException(ErrorCode.MISS_MATCH_PASSWORD);
        }
    }

}
