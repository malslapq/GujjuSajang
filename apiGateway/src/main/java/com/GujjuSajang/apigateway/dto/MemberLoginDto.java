package com.GujjuSajang.apigateway.dto;

import com.GujjuSajang.apigateway.type.MemberRole;
import lombok.*;

public class MemberLoginDto {


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Request {
        private String mail;
        private String password;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class Response {
        private Long id;
        private String mail;
        private boolean mailVerified;
        private MemberRole role;

    }


}
