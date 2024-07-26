package com.GujjuSajang.member.controller;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.util.RequestHeaderUtil;
import com.GujjuSajang.member.dto.MemberDetailDto;
import com.GujjuSajang.member.dto.MemberLoginDto;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import com.GujjuSajang.member.dto.MemberUpdatePasswordDto;
import com.GujjuSajang.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입",
            description = "새로운 회원을 등록합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "회원가입 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/signup")
    public ResponseEntity<MemberSignUpDto.Response> signUp(
            @RequestBody @Valid MemberSignUpDto.Request memberSignUpDto) {
        return ResponseEntity.ok().body(memberService.signUp(memberSignUpDto));
    }

    @Operation(summary = "로그인",
            description = "회원 로그인을 처리합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PostMapping("/login")
    public ResponseEntity<MemberLoginDto.Response> login(
            @RequestBody @Valid MemberLoginDto.Request memberLoginDto) {
        return ResponseEntity.ok().body(memberService.login(memberLoginDto));
    }

    @Operation(summary = "메일 검증",
            description = "회원 가입 시 사용한 메일을 검증합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메일 검증 성공")
            })
    @GetMapping("/mail-verified")
    public void mailVerified(
            @Parameter(description = "회원 ID") @RequestParam Long id,
            @Parameter(description = "검증 코드") @RequestParam String code) {
        memberService.verifiedMail(id, code);
    }

    @Operation(summary = "회원 상세 정보 조회",
            description = "회원의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "상세 정보 조회 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @GetMapping("/detail")
    public ResponseEntity<MemberDetailDto.Response> getDetail(HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.getDetail(tokenMemberInfo.getId()));
    }

    @Operation(summary = "회원 정보 수정",
            description = "회원의 정보를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정보 수정 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PatchMapping("/detail")
    public ResponseEntity<MemberDetailDto.Response> updateDetail(
            @RequestBody @Valid MemberDetailDto.Request memberDetailDto,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.updateConsumer(tokenMemberInfo.getId(), memberDetailDto));
    }

    @Operation(summary = "비밀번호 수정",
            description = "회원의 비밀번호를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비밀번호 수정 성공",
                            content = @Content(mediaType = "application/json"))
            })
    @PatchMapping("/detail/password")
    public ResponseEntity<MemberUpdatePasswordDto.Response> updatePassword(
            @RequestBody @Valid MemberUpdatePasswordDto.Request memberUpdatePasswordDto,
            HttpServletRequest request) {
        TokenMemberInfo tokenMemberInfo = RequestHeaderUtil.parseTokenMemberInfo(request);
        return ResponseEntity.ok().body(memberService.updatePassword(tokenMemberInfo.getId(), memberUpdatePasswordDto));
    }

}
