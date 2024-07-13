package com.GujjuSajang.member.dto;

import com.GujjuSajang.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class MemberDetailDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Request {

        @NotBlank
        private String password;
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

        private Long id;
        @NotBlank
        private String name;
        @NotBlank
        private String mail;
        @NotBlank
        private String phone;
        @NotBlank
        private String address;

        public static MemberDetailDto.Response from(Member member) {
            return MemberDetailDto.Response.builder()
                    .id(member.getId())
                    .name(member.getName())
                    .mail(member.getMail())
                    .phone(member.getPhone())
                    .address(member.getAddress())
                    .build();
        }

    }


}
