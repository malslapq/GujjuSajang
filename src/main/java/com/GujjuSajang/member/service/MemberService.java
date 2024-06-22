package com.GujjuSajang.member.service;

import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.Jwt.service.JwtService;
import com.GujjuSajang.exception.ConsumerException;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberUpdateDetailDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.type.MemberRole;
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
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;
    private final MailVerifiedRepository mailVerifiedRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // 회원 가입
    @Transactional
    public TokenInfo signUp(MemberSignUpDto memberSignUpDto) {

        String encodedPassword = passwordEncoder.encode(memberSignUpDto.getPassword());

        Member member = memberRepository.save(
                Member.builder()
                        .name(memberSignUpDto.getName())
                        .password(encodedPassword)
                        .mail(memberSignUpDto.getMail())
                        .mailVerified(false)
                        .phone(memberSignUpDto.getPhone())
                        .address(memberSignUpDto.getAddress())
                        .role(MemberRole.MEMBER)
                        .build()
        );

        try {
            mailVerifiedRepository.save(member.getId(), mailSender.sendVerifiedMail(member.getId(), member.getMail()));
        } catch (MessagingException e) {
            throw new ConsumerException(ErrorCode.FAIL_SEND_MAIL);
        }

        return jwtService.issueTokens(TokenMemberInfo.builder()
                .id(member.getId())
                .mail(member.getMail())
                .mailVerified(member.isMailVerified())
                .build()
        );
    }

    // 로그인
    public TokenInfo login(MemberLoginDto memberLoginDto) {
        Member member = memberRepository.findByMail(memberLoginDto.getMail()).orElseThrow(() ->
                new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword());
        matchPassword(memberLoginDto.getPassword(), member.getPassword());
        return jwtService.issueTokens(TokenMemberInfo.builder()
                .id(member.getId())
                .mail(member.getMail())
                .mailVerified(member.isMailVerified())
                .role(MemberRole.MEMBER)
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
        Member member = memberRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        String getCode = mailVerifiedRepository.getCode(id).orElseThrow(() -> new ConsumerException(ErrorCode.INVALID_CODE));
        if (code.equals(getCode)) {
            member.changeMailVerified(true);
            mailVerifiedRepository.delete(id);
        }
    }

    // 회원 상세 조회
    @Transactional(readOnly = true)
    public MemberUpdateDetailDto getDetail(long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        return Member.from(member);
    }

    // 회원 정보 수정
    @Transactional
    public MemberUpdateDetailDto updateConsumer(Long id, Long tokenId, MemberUpdateDetailDto memberUpdateDetailDto) {
        verifyId(tokenId, id);
        Member member = memberRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        matchPassword(memberUpdateDetailDto.getPassword(), member.getPassword());
        member.changeAddressAndPhone(memberUpdateDetailDto.getAddress(), memberUpdateDetailDto.getPhone());
        return Member.from(member);
    }

    // 비밀번호 변경
    @Transactional
    public MemberUpdatePasswordDto.Response updatePassword(Long id, Long tokenId, MemberUpdatePasswordDto memberUpdatePasswordDto) {
        verifyId(id, tokenId);
        Member member = memberRepository.findById(id).orElseThrow(() -> new ConsumerException(ErrorCode.NOT_FOUND_CONSUMER));
        matchPassword(memberUpdatePasswordDto.getCurPassword(), member.getPassword());
        member.changePassword(passwordEncoder.encode(memberUpdatePasswordDto.getNewPassword()));
        return MemberUpdatePasswordDto.Response.builder()
                .id(member.getId())
                .name(member.getName())
                .mail(member.getMail())
                .phone(member.getPhone())
                .address(member.getAddress())
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
