package com.GujjuSajang.member.entity;

import com.GujjuSajang.member.dto.SellerDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Seller extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long memberId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String contactNumber;
    @Column(nullable = false)
    private String registrationNumber;


    public static Seller from(SellerDto sellerDto) {
        return Seller.builder()
                .memberId(sellerDto.getMemberId())
                .name(sellerDto.getName())
                .address(sellerDto.getAddress())
                .contactNumber(sellerDto.getContactNumber())
                .registrationNumber(sellerDto.getRegistrationNumber())
                .build();
    }

}
