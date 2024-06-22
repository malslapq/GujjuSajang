package com.GujjuSajang.member.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MemberLoginDto {

    private String mail;
    private String password;

}
