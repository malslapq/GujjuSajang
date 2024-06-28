package com.GujjuSajang.member.seller.entity;

import com.GujjuSajang.member.seller.dto.SellerDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Seller {

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
    @Column(updatable = false, nullable = false)
    private LocalDateTime createAt;
    @LastModifiedDate
    @Column
    private LocalDateTime updateAt;


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
