package com.GujjuSajang.Consumer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsumerUpdateDetailDto {

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

}
