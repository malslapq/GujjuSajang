package com.GujjuSajang.cart.dto;

import com.GujjuSajang.cart.type.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TokenMemberInfo {

    private Long id;
    private String mail;
    private boolean mailVerified;
    private MemberRole role;

}
