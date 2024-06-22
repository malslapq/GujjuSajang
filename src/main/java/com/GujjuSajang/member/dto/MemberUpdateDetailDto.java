package com.GujjuSajang.member.dto;

import com.GujjuSajang.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberUpdateDetailDto {

    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String password;
    @NotBlank
    private String mail;
    @NotBlank
    private String phone;
    @NotBlank
    private String address;

    public static MemberUpdateDetailDto from(Member member) {
        return MemberUpdateDetailDto.builder()
                .id(member.getId())
                .name(member.getName())
                .mail(member.getMail())
                .phone(member.getPhone())
                .address(member.getAddress())
                .build();
    }

}
