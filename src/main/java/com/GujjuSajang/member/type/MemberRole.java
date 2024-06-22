package com.GujjuSajang.member.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {

    MEMBER("회원"),
    SELLER("판매자");

    private final String role;
}
