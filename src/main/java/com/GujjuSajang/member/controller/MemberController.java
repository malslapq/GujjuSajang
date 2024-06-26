package com.GujjuSajang.member.controller;

import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.Jwt.service.JwtService;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberUpdateDetailDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.GujjuSajang.Jwt.util.JwtUtil.COOKIE_NAME;
import static com.GujjuSajang.Jwt.util.JwtUtil.getTokenMemberInfo;

@RequestMapping("/member")
@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;
    private final JwtService jwtService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<MemberSignUpDto> signUp(@RequestBody @Valid MemberSignUpDto memberSignUpDto) {
        return ResponseEntity.ok().body(memberService.signUp(memberSignUpDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@RequestBody @Valid MemberLoginDto memberLoginDto, HttpServletResponse response) {
        TokenInfo tokenInfo = memberService.login(memberLoginDto);
        addCookie(response, tokenInfo.getAccessToken(), 30);
        return ResponseEntity.ok().body(tokenInfo);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        memberService.logout(tokenMemberInfo.getId());
        addCookie(response, "", 0);
        return ResponseEntity.ok().body("로그아웃 성공");
    }

    // 토큰 재발급
    @PostMapping("/token/refresh")
    public ResponseEntity<TokenInfo> refreshToken(@RequestBody String refreshToken, HttpServletResponse response) {
        TokenInfo tokenInfo = jwtService.refreshToken(refreshToken);
        addCookie(response, tokenInfo.getAccessToken(), 30);
        return ResponseEntity.ok(tokenInfo);
    }

    // 메일 검증
    @GetMapping("/mailVerified")
    public void mailVerified(@RequestParam Long id, @RequestParam String code) {
        memberService.verifiedMail(id, code);
    }

    // 상세 정보 조회
    @GetMapping("/detail/{member-id}")
    public ResponseEntity<MemberUpdateDetailDto> getDetail(@PathVariable("member-id") long id) {
        return ResponseEntity.ok().body(memberService.getDetail(id));
    }

    // 정보 수정
    @PatchMapping("/detail/{member-id}")
    public ResponseEntity<MemberUpdateDetailDto> updateDetail(@PathVariable("member-id") Long id, @RequestBody @Valid MemberUpdateDetailDto memberUpdateDetailDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.updateConsumer(id, tokenMemberInfo.getId(), memberUpdateDetailDto));
    }

    // 비밀번호 수정
    @PatchMapping("/detail/{member-id}/password")
    public ResponseEntity<MemberUpdatePasswordDto.Response> updatePassword(@PathVariable("member-id") Long id, @RequestBody @Valid MemberUpdatePasswordDto memberUpdatePasswordDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = getTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.updatePassword(id, tokenMemberInfo.getId(), memberUpdatePasswordDto));
    }

    private void addCookie(HttpServletResponse response, String accessToken, int expiryMinutes) {
        Cookie cookie = new Cookie(COOKIE_NAME, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * expiryMinutes);
        response.addCookie(cookie);
    }

}
