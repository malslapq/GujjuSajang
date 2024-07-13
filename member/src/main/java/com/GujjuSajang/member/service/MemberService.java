package com.GujjuSajang.member.service;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberDetailDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MailVerifiedRedisRepository;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailVerifiedRedisRepository mailVerifiedRedisRepository;

    // 로그인
    public MemberLoginDto.Response login(MemberLoginDto.Request memberLoginDto) {
        Member member = memberRepository.findByMail(memberLoginDto.getMail()).orElseThrow(() ->
                new MemberException(ErrorCode.NOT_FOUND_MEMBER));

        passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword());
        matchPassword(memberLoginDto.getPassword(), member.getPassword());

        return MemberLoginDto.Response.builder()
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
    public MemberDetailDto.Response getDetail(Long id) {
        return MemberDetailDto.Response.from(getMember(id));
    }

    // 회원 정보 수정
    @Transactional
    public MemberDetailDto.Response updateConsumer(Long id, MemberDetailDto.Request memberDetailDto) {
        Member member = getMember(id);
        matchPassword(memberDetailDto.getPassword(), member.getPassword());
        member.changeAddressAndPhone(memberDetailDto.getAddress(), memberDetailDto.getPhone());
        return MemberDetailDto.Response.from(member);
    }

    // 비밀번호 변경
    @Transactional
    public MemberUpdatePasswordDto.Response updatePassword(Long id, MemberUpdatePasswordDto.Request memberUpdatePasswordDto) {
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

}
