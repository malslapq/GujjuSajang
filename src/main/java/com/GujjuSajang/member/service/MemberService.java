package com.GujjuSajang.member.service;

import com.GujjuSajang.Jwt.Repository.RefreshTokenRedisRepository;
import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.Jwt.service.JwtService;
import com.GujjuSajang.cart.dto.CartDto;
import com.GujjuSajang.cart.repository.CartRedisRepository;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.MemberException;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberUpdateDetailDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MailVerifiedRedisRepository;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.util.Mail.MailSender;
import com.GujjuSajang.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MailSender mailSender;
    private final MailVerifiedRedisRepository mailVerifiedRedisRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final CartRedisRepository cartRedisRepository;

    // 회원 가입
    @Transactional
    public MemberSignUpDto signUp(MemberSignUpDto memberSignUpDto) {

        String encodedPassword = passwordEncoder.encode(memberSignUpDto.getPassword());
        Member member;

        try {
            member = memberRepository.save(Member.from(memberSignUpDto, encodedPassword));
        } catch (DataIntegrityViolationException e) {
            throw new MemberException(ErrorCode.ALREADY_MAIL, e);
        }

        mailVerifiedRedisRepository.save(member.getId(), mailSender.sendVerifiedMail(member.getId(), member.getMail()));

        cartRedisRepository.save(member.getId(), new CartDto());

        jwtService.issueTokens(TokenMemberInfo.builder()
                .id(member.getId())
                .mail(member.getMail())
                .mailVerified(member.isMailVerified())
                .build());
        return MemberSignUpDto.from(member);
    }

    // 로그인
    public TokenInfo login(MemberLoginDto memberLoginDto) {

        Member member = memberRepository.findByMail(memberLoginDto.getMail()).orElseThrow(() ->
                new MemberException(ErrorCode.NOT_FOUND_MEMBER));

        passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword());
        matchPassword(memberLoginDto.getPassword(), member.getPassword());

        return jwtService.issueTokens(TokenMemberInfo.builder()
                .id(member.getId())
                .mail(member.getMail())
                .mailVerified(member.isMailVerified())
                .role(member.getRole())
                .build()
        );
    }

    // 로그아웃
    public void logout(Long id) {
        refreshTokenRedisRepository.delete(id);
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
    public MemberUpdateDetailDto getDetail(long id) {
        Member member = getMember(id);
        return MemberUpdateDetailDto.from(member);
    }

    // 회원 정보 수정
    @Transactional
    public MemberUpdateDetailDto updateConsumer(Long id, Long tokenId, MemberUpdateDetailDto memberUpdateDetailDto) {
        validateMemberId(tokenId, id);
        Member member = getMember(id);
        matchPassword(memberUpdateDetailDto.getPassword(), member.getPassword());
        member.changeAddressAndPhone(memberUpdateDetailDto.getAddress(), memberUpdateDetailDto.getPhone());
        return MemberUpdateDetailDto.from(member);
    }

    // 비밀번호 변경
    @Transactional
    public MemberUpdatePasswordDto.Response updatePassword(Long id, Long tokenId, MemberUpdatePasswordDto memberUpdatePasswordDto) {
        validateMemberId(id, tokenId);
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

    // id 검증
    public static void validateMemberId(Long memberId, Long tokenId) {
        if (!memberId.equals(tokenId)) {
            throw new MemberException(ErrorCode.MISS_MATCH_MEMBER);
        }
    }

    // 비밀번호 검증
    public void matchPassword(String requestPassword, String encodedPassword) {
        if (!passwordEncoder.matches(requestPassword, encodedPassword)) {
            throw new MemberException(ErrorCode.MISS_MATCH_PASSWORD);
        }
    }

}
