package com.GujjuSajang.apigateway.controller;

import com.GujjuSajang.apigateway.dto.TokenInfo;
import com.GujjuSajang.apigateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final JwtService jwtService;

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenInfo> refreshAccessToken(@RequestBody String refreshToken) {
        return ResponseEntity.ok().body(jwtService.refreshToken(refreshToken));
    }

}
