package com.GujjuSajang.member.dto;

import com.GujjuSajang.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class MemberSignUpDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        @NotBlank
        private String name;
        @NotBlank
        private String password;
        @NotBlank
        @Email
        private String mail;
        @NotBlank
        private String phone;
        @NotBlank
        private String address;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Response {

        @NotBlank
        private String name;
        @NotBlank
        @Email
        private String mail;
        @NotBlank
        private String phone;
        @NotBlank
        private String address;

        public static MemberSignUpDto.Response from(Member member) {
            return MemberSignUpDto.Response.builder()
                    .name(member.getName())
                    .mail(member.getMail())
                    .phone(member.getPhone())
                    .address(member.getAddress())
                    .build();
        }

    }


}
