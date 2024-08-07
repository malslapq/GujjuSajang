package com.GujjuSajang.member.entity;

import com.GujjuSajang.core.type.MemberRole;
import com.GujjuSajang.member.dto.MemberSignUpDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
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

    public void changeRole(MemberRole role) {
        this.role = role;
    }

    public static Member of(MemberSignUpDto.Request memberSignUpDto, String encodedPassword) {
        return Member.builder()
                .name(memberSignUpDto.getName())
                .password(encodedPassword)
                .mail(memberSignUpDto.getMail())
                .mailVerified(false)
                .phone(memberSignUpDto.getPhone())
                .address(memberSignUpDto.getAddress())
                .role(MemberRole.MEMBER)
                .build();
    }

}

