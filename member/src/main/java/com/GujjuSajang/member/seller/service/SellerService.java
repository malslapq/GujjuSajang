package com.GujjuSajang.member.seller.service;

import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import com.GujjuSajang.core.type.MemberRole;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.seller.dto.SellerDto;
import com.GujjuSajang.member.seller.entity.Seller;
import com.GujjuSajang.member.seller.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final MemberRepository memberRepository;

    // 판매자 등록
    @Transactional
    public SellerDto createSeller(Long memberId, SellerDto sellerDto) {
        sellerDto.setMemberId(memberId);
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));
        member.changeRole(MemberRole.SELLER);
        return SellerDto.from(sellerRepository.save(Seller.from(sellerDto)));
    }

}
