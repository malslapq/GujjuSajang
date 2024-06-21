package com.GujjuSajang.Consumer.controller;

import com.GujjuSajang.Consumer.dto.ConsumerLoginDto;
import com.GujjuSajang.Consumer.dto.ConsumerUpdateDetailDto;
import com.GujjuSajang.Consumer.dto.ConsumerSignUpDto;
import com.GujjuSajang.Consumer.dto.ConsumerUpdatePasswordDto;
import com.GujjuSajang.Consumer.service.ConsumerService;
import com.GujjuSajang.Jwt.dto.TokenInfo;
import com.GujjuSajang.Jwt.dto.TokenUserInfo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/consumer")
@RequiredArgsConstructor
@RestController
public class ConsumerController {

    private final ConsumerService consumerService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<TokenInfo> signUp(@RequestBody @Valid ConsumerSignUpDto consumerSignUpDto) {
        return ResponseEntity.ok().body(consumerService.signUp(consumerSignUpDto));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenInfo> login(@RequestBody @Valid ConsumerLoginDto consumerLoginDto) {
        return ResponseEntity.ok().body(consumerService.login(consumerLoginDto));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        TokenUserInfo tokenUserInfo = (TokenUserInfo) request.getAttribute("tokenUserInfo");
        consumerService.logout(tokenUserInfo.getId());
        return ResponseEntity.ok().body("로그아웃 성공");
    }

    // 메일 검증
    @GetMapping("/mailVerified")
    public void mailVerified(@RequestParam Long id, @RequestParam String code) {
        consumerService.verifiedMail(id, code);
    }

    // 상세 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<ConsumerUpdateDetailDto> getDetail(@PathVariable long id) {
        return ResponseEntity.ok().body(consumerService.getDetail(id));
    }

    // 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ConsumerUpdateDetailDto> updateDetail(@PathVariable Long id, @RequestBody @Valid ConsumerUpdateDetailDto consumerUpdateDetailDto, HttpServletRequest request) {
        TokenUserInfo tokenUserInfo = (TokenUserInfo) request.getAttribute("tokenUserInfo");
        return ResponseEntity.ok().body(consumerService.updateConsumer(id, tokenUserInfo.getId(), consumerUpdateDetailDto));
    }

    // 비밀번호 수정
    @PatchMapping("/{id}/password")
    public ResponseEntity<ConsumerUpdatePasswordDto.Response> updatePassword(@PathVariable Long id, @RequestBody @Valid ConsumerUpdatePasswordDto consumerUpdatePasswordDto, HttpServletRequest request) {
        TokenUserInfo tokenUserInfo = (TokenUserInfo) request.getAttribute("tokenUserInfo");
        return ResponseEntity.ok().body(consumerService.updatePassword(id, tokenUserInfo.getId(), consumerUpdatePasswordDto));
    }

}
