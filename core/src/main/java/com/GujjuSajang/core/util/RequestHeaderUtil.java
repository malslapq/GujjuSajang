package com.GujjuSajang.core.util;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.TokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestHeaderUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static TokenMemberInfo parseTokenMemberInfo(HttpServletRequest request)  {
        try {
            String tokenMemberInfo = request.getHeader("tokenMemberInfo");
            return objectMapper.readValue(tokenMemberInfo, TokenMemberInfo.class);
        } catch (JsonProcessingException e) {
            throw new TokenException(ErrorCode.FAIL_JSON_PARSE, e);
        }
    }

}
