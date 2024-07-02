package com.GujjuSajang.apigateway.dto;

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

}
