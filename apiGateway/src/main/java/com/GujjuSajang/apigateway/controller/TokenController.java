package com.GujjuSajang.apigateway.controller;

import com.GujjuSajang.apigateway.dto.RefreshTokenDto;
import com.GujjuSajang.apigateway.dto.TokenInfo;
import com.GujjuSajang.apigateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final AuthService authService;

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenInfo> refreshAccessToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        return ResponseEntity.ok().body(authService.refreshToken(refreshTokenDto));
    }

    @PostMapping("/token/logout")
    public ResponseEntity<?> logout(@RequestBody TokenInfo tokenInfo) {
        return ResponseEntity.ok("");
    }

}
