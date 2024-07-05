package com.GujjuSajang.member.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberUpdateDetailDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.event.MemberEventProducer;
import com.GujjuSajang.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;
    private final MemberEventProducer memberEventProducer;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<MemberSignUpDto> signUp(@RequestBody @Valid MemberSignUpDto memberSignUpDto) {
        return ResponseEntity.ok().body(memberEventProducer.signUp(memberSignUpDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenMemberInfo> login(@RequestBody @Valid MemberLoginDto memberLoginDto) {
        return ResponseEntity.ok().body(memberService.login(memberLoginDto));
    }

    // 메일 검증
    @GetMapping("/mail-verified")
    public void mailVerified(@RequestParam Long id, @RequestParam String code) {
        memberService.verifiedMail(id, code);
    }

    // 상세 정보 조회
    @GetMapping("/detail")
    public ResponseEntity<MemberUpdateDetailDto> getDetail(HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.getDetail(tokenMemberInfo.getId()));
    }

    // 정보 수정
    @PatchMapping("/detail")
    public ResponseEntity<MemberUpdateDetailDto> updateDetail(@RequestBody @Valid MemberUpdateDetailDto memberUpdateDetailDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.updateConsumer(tokenMemberInfo.getId(), memberUpdateDetailDto));
    }

    // 비밀번호 수정
    @PatchMapping("/detail/password")
    public ResponseEntity<MemberUpdatePasswordDto.Response> updatePassword(@RequestBody @Valid MemberUpdatePasswordDto memberUpdatePasswordDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.updatePassword(tokenMemberInfo.getId(), memberUpdatePasswordDto));
    }

}
