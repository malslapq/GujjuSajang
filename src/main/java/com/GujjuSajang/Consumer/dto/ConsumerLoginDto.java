package com.GujjuSajang.Consumer.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ConsumerLoginDto {

    private String mail;
    private String password;

}
