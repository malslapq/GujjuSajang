package com.GujjuSajang.member.dto;

import com.GujjuSajang.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberSignUpDto {

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

    public static MemberSignUpDto from(Member member) {
        return MemberSignUpDto.builder()
                .name(member.getName())
                .mail(member.getMail())
                .phone(member.getPhone())
                .address(member.getAddress())
                .build();
    }

}
