package com.GujjuSajang.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class MemberUpdatePasswordDto {

    @NotBlank
    private String curPassword;
    @NotBlank
    private String newPassword;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class Response{
        private Long id;
        private String name;
        private String mail;
        private String phone;
        private String address;
    }

}
