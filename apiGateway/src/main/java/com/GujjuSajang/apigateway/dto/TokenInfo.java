package com.GujjuSajang.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class TokenInfo {

    private String accessToken;
    private String refreshToken;
    private String prefix;

}
