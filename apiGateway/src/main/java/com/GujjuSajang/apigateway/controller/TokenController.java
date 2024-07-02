package com.GujjuSajang.apigateway.controller;

import com.GujjuSajang.apigateway.dto.RefreshTokenDto;
import com.GujjuSajang.apigateway.dto.TokenInfo;
import com.GujjuSajang.apigateway.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final JwtService jwtService;

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenInfo> refreshAccessToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return ResponseEntity.ok().body(jwtService.refreshToken(refreshTokenDto));
    }

}
