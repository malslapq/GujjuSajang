package com.GujjuSajang.member.service;

import com.GujjuSajang.core.dto.CreateMemberEventDto;
import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import com.GujjuSajang.core.service.EventProducerService;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberUpdateDetailDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MailVerifiedRedisRepository;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class MemberService {

    @Value("${spring.mail.randomCode}")
    private String CHARACTERS;
    @Value("${spring.mail.codeLength}")
    private int codeLength;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventProducerService eventProducerService;
    private final MailVerifiedRedisRepository mailVerifiedRedisRepository;

    // 회원 가입
    @Transactional
    public MemberSignUpDto signUp(MemberSignUpDto memberSignUpDto) {

        String encodedPassword = passwordEncoder.encode(memberSignUpDto.getPassword());
        Member member;

        member = memberRepository.save(Member.from(memberSignUpDto, encodedPassword));

        eventProducerService.sendEvent("create-member",
                CreateMemberEventDto.builder()
                        .id(member.getId())
                        .mail(member.getMail())
                        .code(getVerifiedCode(codeLength))
                        .build()
        );

        return MemberSignUpDto.from(member);
    }

    // 로그인
    public TokenMemberInfo login(MemberLoginDto memberLoginDto) {
        Member member = memberRepository.findByMail(memberLoginDto.getMail()).orElseThrow(() ->
                new MemberException(ErrorCode.NOT_FOUND_MEMBER));

        passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword());
        matchPassword(memberLoginDto.getPassword(), member.getPassword());

        return TokenMemberInfo.builder()
                .id(member.getId())
                .mail(member.getMail())
                .mailVerified(member.isMailVerified())
                .role(member.getRole())
                .build();
    }

    // 메일 인증
    @Transactional
    public void verifiedMail(Long id, String code) {
        Member member = getMember(id);
        String getCode = mailVerifiedRedisRepository.getCode(id).orElseThrow(() -> new MemberException(ErrorCode.INVALID_CODE));

        if (code.equals(getCode)) {
            member.changeMailVerified(true);
            mailVerifiedRedisRepository.delete(id);
        }
    }

    // 회원 상세 조회
    @Transactional(readOnly = true)
    public MemberUpdateDetailDto getDetail(Long id) {
        return MemberUpdateDetailDto.from(getMember(id));
    }

    // 회원 정보 수정
    @Transactional
    public MemberUpdateDetailDto updateConsumer(Long id, MemberUpdateDetailDto memberUpdateDetailDto) {
        Member member = getMember(id);
        matchPassword(memberUpdateDetailDto.getPassword(), member.getPassword());
        member.changeAddressAndPhone(memberUpdateDetailDto.getAddress(), memberUpdateDetailDto.getPhone());
        return MemberUpdateDetailDto.from(member);
    }

    // 비밀번호 변경
    @Transactional
    public MemberUpdatePasswordDto.Response updatePassword(Long id, MemberUpdatePasswordDto memberUpdatePasswordDto) {
        Member member = getMember(id);
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

    // 회원 가져오기
    public Member getMember(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));
    }

    // 비밀번호 검증
    public void matchPassword(String requestPassword, String encodedPassword) {
        if (!passwordEncoder.matches(requestPassword, encodedPassword)) {
            throw new MemberException(ErrorCode.MISS_MATCH_PASSWORD);
        }
    }

    private String getVerifiedCode(int codeLength) {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

}
