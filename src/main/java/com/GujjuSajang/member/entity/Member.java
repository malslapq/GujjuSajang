package com.GujjuSajang.member.entity;

import com.GujjuSajang.member.dto.MemberUpdateDetailDto;
import com.GujjuSajang.member.type.MemberRole;
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
public class Member extends BaseTimeEntity {

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
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public void changeMailVerified(boolean verified) {
        this.mailVerified = verified;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeAddressAndPhone(String address, String phone) {
        this.address = address;
        this.phone = phone;
    }

}

