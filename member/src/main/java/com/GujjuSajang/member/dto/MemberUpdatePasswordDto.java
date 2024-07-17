package com.GujjuSajang.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class MemberUpdatePasswordDto {

    @Builder
    @Getter
    @Setter
    public static class Request {

        @NotBlank
        private String curPassword;
        @NotBlank
        private String newPassword;

    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class Response {

        private Long id;
        private String name;
        private String mail;
        private String phone;
        private String address;

    }

}
