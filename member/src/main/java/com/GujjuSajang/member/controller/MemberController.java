package com.GujjuSajang.member.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberDetailDto;
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
    public ResponseEntity<MemberSignUpDto.Response> signUp(@RequestBody @Valid MemberSignUpDto.Request memberSignUpDto) {
        return ResponseEntity.ok().body(memberEventProducer.signUp(memberSignUpDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<MemberLoginDto.Response> login(@RequestBody @Valid MemberLoginDto.Request memberLoginDto) {
        return ResponseEntity.ok().body(memberService.login(memberLoginDto));
    }

    // 메일 검증
    @GetMapping("/mail-verified")
    public void mailVerified(@RequestParam Long id, @RequestParam String code) {
        memberService.verifiedMail(id, code);
    }

    // 상세 정보 조회
    @GetMapping("/detail")
    public ResponseEntity<MemberDetailDto.Response> getDetail(HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.getDetail(tokenMemberInfo.getId()));
    }

    // 정보 수정
    @PatchMapping("/detail")
    public ResponseEntity<MemberDetailDto.Response> updateDetail(@RequestBody @Valid MemberDetailDto.Request memberDetailDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.updateConsumer(tokenMemberInfo.getId(), memberDetailDto));
    }

    // 비밀번호 수정
    @PatchMapping("/detail/password")
    public ResponseEntity<MemberUpdatePasswordDto.Response> updatePassword(@RequestBody @Valid MemberUpdatePasswordDto.Request memberUpdatePasswordDto, HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.updatePassword(tokenMemberInfo.getId(), memberUpdatePasswordDto));
    }

}
