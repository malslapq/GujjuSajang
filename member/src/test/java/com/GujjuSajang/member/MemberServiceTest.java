package com.GujjuSajang.member;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import com.GujjuSajang.core.type.MemberRole;
import com.GujjuSajang.member.dto.MemberDetailDto;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.event.MemberEventProducer;
import com.GujjuSajang.member.repository.MailVerifiedRedisRepository;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.service.MemberService;
import com.GujjuSajang.member.util.MailSender;
import com.GujjuSajang.member.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {


    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailVerifiedRedisRepository mailVerifiedRedisRepository;
    @Mock
    private MemberEventProducer memberEventProducer;
    @InjectMocks
    private MemberService memberService;

    Member member;
    MemberSignUpDto.Request memberSignUpDtoRequest;
    MemberLoginDto.Request memberLoginDtoRequest;

    @BeforeEach
    void init() {
        member = Member.builder()
                .id(1L)
                .name("name")
                .phone("phone")
                .address("address")
                .password("encodedPassword")
                .mail("mail@example.com")
                .mailVerified(false)
                .role(MemberRole.MEMBER)
                .build();
        memberSignUpDtoRequest = MemberSignUpDto.Request.builder()
                .name("name")
                .phone("phone")
                .mail("mail@example.com")
                .address("address")
                .password("encodedPassword")
                .build();
        memberLoginDtoRequest = MemberLoginDto.Request.builder()
                .mail("mail@example.com")
                .password("password")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        given(passwordEncoder.encode(memberSignUpDtoRequest.getPassword())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(member);

        // when
        MemberSignUpDto.Response response = memberService.signUp(memberSignUpDtoRequest);

        // then
        assertThat(response.getName()).isEqualTo(member.getName());
        assertThat(response.getPhone()).isEqualTo(member.getPhone());
        assertThat(response.getMail()).isEqualTo(member.getMail());
        assertThat(response.getAddress()).isEqualTo(member.getAddress());
    }

    @DisplayName("회원 가입 실패 - 이메일 중복")
    @Test
    void signUp_Fail_AlreadyMail() {
        //given
        given(passwordEncoder.encode(memberSignUpDtoRequest.getPassword())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willThrow(new DataIntegrityViolationException("Duplicate entry"));

        //when


        //then
        assertThatThrownBy(() -> memberService.signUp(memberSignUpDtoRequest))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.ALREADY_MAIL.getErrorMessage());
    }

    @DisplayName("로그인 성공")
    @Test
    void login_success() {
        // given
        given(memberRepository.findByMail(memberLoginDtoRequest.getMail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(memberLoginDtoRequest.getPassword(), member.getPassword())).willReturn(true);

        // when
        MemberLoginDto.Response response = memberService.login(memberLoginDtoRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(member.getId());
        assertThat(response.getMail()).isEqualTo(member.getMail());
        assertThat(response.isMailVerified()).isEqualTo(member.isMailVerified());
        assertThat(response.getRole()).isEqualTo(member.getRole());

    }

    @DisplayName("로그인 실패 - 회원을 찾을 수 없음")
    @Test
    void login_Fail_NotFoundMember() {
        // given
        given(memberRepository.findByMail(memberLoginDtoRequest.getMail())).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> memberService.login(memberLoginDtoRequest))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.NOT_FOUND_MEMBER.getErrorMessage());
    }

    @DisplayName("로그인 실패 - 비밀번호 다름")
    @Test
    void login_Fail_InvalidPassword() {
        // given
        given(memberRepository.findByMail(memberLoginDtoRequest.getMail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(memberLoginDtoRequest.getPassword(), member.getPassword())).willReturn(false);

        // when

        // then
        assertThatThrownBy(() -> memberService.login(memberLoginDtoRequest))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.MISS_MATCH_PASSWORD.getErrorMessage());
    }

    @DisplayName("회원 상세 조회 성공")
    @Test
    void getDetail_Success() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        MemberDetailDto.Response response = memberService.getDetail(member.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(member.getId());
        assertThat(response.getName()).isEqualTo(member.getName());
        assertThat(response.getMail()).isEqualTo(member.getMail());
        assertThat(response.getPhone()).isEqualTo(member.getPhone());
        assertThat(response.getAddress()).isEqualTo(member.getAddress());
    }

    @DisplayName("회원 상세 조회 실패 - 회원을 찾을 수 없음")
    @Test
    void getDetail_Fail_NotFoundMember() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> memberService.getDetail(member.getId()))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.NOT_FOUND_MEMBER.getErrorMessage());
    }

    @DisplayName("비밀번호 변경 성공")
    @Test
    void updatePassword_Success() {
        // given
        MemberUpdatePasswordDto.Request memberUpdatePasswordDto = MemberUpdatePasswordDto.Request.builder()
                .curPassword("curPassword")
                .newPassword("newPassword")
                .build();
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(memberUpdatePasswordDto.getCurPassword(), member.getPassword())).willReturn(true);
        given(passwordEncoder.encode(memberUpdatePasswordDto.getNewPassword())).willReturn("encodedPassword");

        // when
        MemberUpdatePasswordDto.Response result = memberService.updatePassword(member.getId(), memberUpdatePasswordDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(member.getId());
        assertThat(result.getName()).isEqualTo(member.getName());
        assertThat(result.getMail()).isEqualTo(member.getMail());
        assertThat(result.getPhone()).isEqualTo(member.getPhone());
        assertThat(result.getAddress()).isEqualTo(member.getAddress());
        verify(memberRepository).findById(member.getId());
        verify(passwordEncoder).matches(memberUpdatePasswordDto.getCurPassword(), member.getPassword());
        verify(passwordEncoder).encode(memberUpdatePasswordDto.getNewPassword());

    }

    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    @Test
    void updatePassword_Fail_InvalidCurrentPassword() {
        // given
        MemberUpdatePasswordDto.Request memberUpdatePasswordDto = MemberUpdatePasswordDto.Request.builder()
                .curPassword("wrongPassword")
                .newPassword("newPassword")
                .build();
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(memberUpdatePasswordDto.getCurPassword(), member.getPassword())).willReturn(false);

        // when

        // then
        assertThatThrownBy(() -> memberService.updatePassword(member.getId(), memberUpdatePasswordDto))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.MISS_MATCH_PASSWORD.getErrorMessage());
    }

    @DisplayName("메일 인증 성공")
    @Test
    void verifiedMail_Success() {
        // given
        String verificationCode = "verificationCode";
        Member testMember = Member.builder()
                .id(member.getId())
                .name(member.getName())
                .mail(member.getMail())
                .phone(member.getPhone())
                .address(member.getAddress())
                .mailVerified(true)
                .build();

        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(mailVerifiedRedisRepository.getCode(1L)).willReturn(Optional.of(verificationCode));

        // when
        memberService.verifiedMail(testMember.getId(), verificationCode);

        // then
        assertThat(member.isMailVerified()).isTrue();
    }

    @DisplayName("메일 인증 실패 - 잘못된 코드")
    @Test
    void verifiedMail_Fail_InvalidCode() {
        // given
        Long memberId = 1L;
        String wrongCode = "wrongCode";

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        given(mailVerifiedRedisRepository.getCode(memberId)).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> memberService.verifiedMail(memberId, wrongCode))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.INVALID_CODE.getErrorMessage());
    }


}
