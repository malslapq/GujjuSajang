package com.GujjuSajang.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

}
