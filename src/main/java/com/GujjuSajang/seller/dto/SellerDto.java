package com.GujjuSajang.seller.dto;

import com.GujjuSajang.seller.entity.Seller;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SellerDto {

    private Long id;
    private Long memberId;
    private String name;
    private String address;
    private String contactNumber;
    private String registrationNumber;

    public static SellerDto from(Seller seller) {
        return SellerDto.builder()
                .id(seller.getId())
                .memberId(seller.getMemberId())
                .name(seller.getName())
                .address(seller.getAddress())
                .contactNumber(seller.getContactNumber())
                .registrationNumber(seller.getRegistrationNumber())
                .build();
    }

}
