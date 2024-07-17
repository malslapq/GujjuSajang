package com.GujjuSajang.core.dto;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMemberEventDto {

    private Long id;
    private String mail;
    private String code;

}
