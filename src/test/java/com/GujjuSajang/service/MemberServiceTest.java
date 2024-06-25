package com.GujjuSajang.service;

import com.GujjuSajang.Jwt.Repository.RefreshTokenRedisRepository;
import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.Jwt.service.JwtService;
import com.GujjuSajang.cart.repository.CartRedisRepository;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.MemberException;
import com.GujjuSajang.exception.RedisException;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberUpdateDetailDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MailVerifiedRedisRepository;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.service.MemberService;
import com.GujjuSajang.member.type.MemberRole;
import com.GujjuSajang.util.Mail.MailSender;
import com.GujjuSajang.util.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private MailSender mailSender;
    @Mock
    private RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Mock
    private MailVerifiedRedisRepository mailVerifiedRedisRepository;
    @Mock
    private CartRedisRepository cartRedisRepository;
    @InjectMocks
    private MemberService memberService;

    Member member;
    MemberSignUpDto memberSignUpDto;
    MemberLoginDto memberLoginDto;

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
        memberSignUpDto = MemberSignUpDto.builder()
                .name("name")
                .phone("phone")
                .mail("mail@example.com")
                .address("address")
                .password("encodedPassword")
                .build();
        memberLoginDto = MemberLoginDto.builder()
                .mail("mail@example.com")
                .password("password")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void signUp_Success() {
        // given
        given(passwordEncoder.encode(memberSignUpDto.getPassword())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(member);

        // when
        MemberSignUpDto response = memberService.signUp(memberSignUpDto);

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
        given(passwordEncoder.encode(memberSignUpDto.getPassword())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willThrow(new DataIntegrityViolationException("Duplicate entry"));

        //when

        //then
        assertThatThrownBy(() -> memberService.signUp(memberSignUpDto))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.ALREADY_MAIL.getErrorMessage());
    }

    @DisplayName("회원가입 실패 - 인증 메일 발송 실패")
    @Test
    void signUp_Fail_FailedSendMail() {
        //given
        given(passwordEncoder.encode(memberSignUpDto.getPassword())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willReturn(member);
        given(mailSender.sendVerifiedMail(member.getId(), member.getMail())).willThrow(new MemberException(ErrorCode.FAIL_SEND_MAIL));

        //when

        //then
        assertThatThrownBy(() -> memberService.signUp(memberSignUpDto))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.FAIL_SEND_MAIL.getErrorMessage());

    }

    @DisplayName("로그인 성공")
    @Test
    void login_success() {
        // given
        given(memberRepository.findByMail(memberLoginDto.getMail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())).willReturn(true);
        given(jwtService.issueTokens(any(TokenMemberInfo.class))).willReturn(new TokenInfo("accessToken", "refreshToken", "Bearer"));

        // when
        TokenInfo tokenInfo = memberService.login(memberLoginDto);

        // then
        assertThat(tokenInfo).isNotNull();
        assertThat(tokenInfo.getAccessToken()).isEqualTo("accessToken");
        assertThat(tokenInfo.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(tokenInfo.getPrefix()).isEqualTo("Bearer");

    }

    @DisplayName("로그인 실패 - 회원을 찾을 수 없음")
    @Test
    void login_Fail_NotFoundMember() {
        // given
        given(memberRepository.findByMail(memberLoginDto.getMail())).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> memberService.login(memberLoginDto))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.NOT_FOUND_MEMBER.getErrorMessage());
    }

    @DisplayName("로그인 실패 - 비밀번호 다름")
    @Test
    void login_Fail_InvalidPassword() {
        // given
        given(memberRepository.findByMail(memberLoginDto.getMail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(memberLoginDto.getPassword(), member.getPassword())).willReturn(false);

        // when

        // then
        assertThatThrownBy(() -> memberService.login(memberLoginDto))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.MISS_MATCH_PASSWORD.getErrorMessage());
    }

    @DisplayName("로그아웃 성공")
    @Test
    void logout_Success() {
        // given

        // when
        memberService.logout(member.getId());

        // then
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        verify(refreshTokenRedisRepository).delete(captor.capture());
        assertThat(captor.getValue()).isEqualTo(member.getId());
    }

    @DisplayName("로그아웃 실패 - 리프레시 토큰 삭제 실패")
    @Test
    void logout_Fail_DeleteRefreshToken() {
        // given
        doThrow(new RedisException(ErrorCode.REDIS_OPERATION_FAILURE)).when(refreshTokenRedisRepository).delete(member.getId());

        // when

        // then
        assertThatThrownBy(() -> memberService.logout(member.getId()))
                .isInstanceOf(RedisException.class)
                .hasMessage(ErrorCode.REDIS_OPERATION_FAILURE.getErrorMessage());
    }

    @DisplayName("회원 상세 조회 성공")
    @Test
    void getDetail_Success() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));

        // when
        MemberUpdateDetailDto result = memberService.getDetail(member.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(member.getId());
        assertThat(result.getName()).isEqualTo(member.getName());
        assertThat(result.getMail()).isEqualTo(member.getMail());
        assertThat(result.getPhone()).isEqualTo(member.getPhone());
        assertThat(result.getAddress()).isEqualTo(member.getAddress());
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
        MemberUpdatePasswordDto memberUpdatePasswordDto = MemberUpdatePasswordDto.builder()
                .curPassword("curPassword")
                .newPassword("newPassword")
                .build();
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(memberUpdatePasswordDto.getCurPassword(), member.getPassword())).willReturn(true);
        given(passwordEncoder.encode(memberUpdatePasswordDto.getNewPassword())).willReturn("encodedPassword");

        // when
        MemberUpdatePasswordDto.Response result = memberService.updatePassword(member.getId(), member.getId(), memberUpdatePasswordDto);

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
        MemberUpdatePasswordDto memberUpdatePasswordDto = MemberUpdatePasswordDto.builder()
                .curPassword("wrongPassword")
                .newPassword("newPassword")
                .build();
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(memberUpdatePasswordDto.getCurPassword(), member.getPassword())).willReturn(false);

        // when

        // then
        assertThatThrownBy(() -> memberService.updatePassword(member.getId(), member.getId(), memberUpdatePasswordDto))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.MISS_MATCH_PASSWORD.getErrorMessage());
    }

    @DisplayName("회원 ID 검증 성공")
    @Test
    public void validateMemberId_Success() {
        // given
        Long memberId = 1L;
        Long tokenId = 1L;

        // when

        // then
        MemberService.validateMemberId(memberId, tokenId);
    }

    @DisplayName("회원 ID 검증 실패 - 불일치")
    @Test
    public void validateMemberId_Fail_Mismatch() {
        // given
        Long memberId = 1L;
        Long tokenId = 2L;

        // when

        // then
        assertThatThrownBy(() -> MemberService.validateMemberId(memberId, tokenId))
                .isInstanceOf(MemberException.class)
                .hasMessage(ErrorCode.MISS_MATCH_MEMBER.getErrorMessage());
    }

}
