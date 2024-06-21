package com.GujjuSajang.Consumer.entity;

import com.GujjuSajang.Consumer.dto.ConsumerDetailDto;
import com.GujjuSajang.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Consumer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String mail;
    @Column(nullable = false)
    private boolean mailVerified;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String address;

    public void changeMailVerified(boolean verified) {
        this.mailVerified = verified;
    }

    public static ConsumerDetailDto from(Consumer consumer) {
        return ConsumerDetailDto.builder()
                .id(consumer.getId())
                .name(consumer.getName())
                .mail(consumer.mail)
                .phone(consumer.phone)
                .address(consumer.address)
                .build();
    }

}

