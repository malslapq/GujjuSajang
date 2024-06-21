package com.GujjuSajang.Consumer.controller;

import com.GujjuSajang.Consumer.dto.ConsumerDetailDto;
import com.GujjuSajang.Consumer.dto.ConsumerSignUpDto;
import com.GujjuSajang.Consumer.service.ConsumerService;
import com.GujjuSajang.Jwt.dto.TokenInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/consumer")
@RequiredArgsConstructor
@RestController
public class ConsumerController {

    private final ConsumerService consumerService;

    @PostMapping("/signup")
    public ResponseEntity<TokenInfo> signUp(@RequestBody @Valid ConsumerSignUpDto consumerSignUpDto) {
        return ResponseEntity.ok().body(consumerService.signUp(consumerSignUpDto));
    }

    @GetMapping("/mailVerified")
    public void mailVerified(@RequestParam long id, @RequestParam String code) {
        consumerService.verifiedMail(id, code);
    }





}
