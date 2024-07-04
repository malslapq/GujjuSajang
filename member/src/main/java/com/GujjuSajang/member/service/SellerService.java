package com.GujjuSajang.member.service;

import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.dto.UpdateStockDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import com.GujjuSajang.core.service.EventProducerService;
import com.GujjuSajang.core.type.MemberRole;
import com.GujjuSajang.member.dto.ProductStockUpdateDto;
import com.GujjuSajang.member.dto.SellerDto;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.entity.Seller;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final MemberRepository memberRepository;
    private final EventProducerService eventProducerService;

    // 판매자 등록
    @Transactional
    public SellerDto createSeller(Long memberId, SellerDto sellerDto) {
        sellerDto.setMemberId(memberId);
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new MemberException(ErrorCode.NOT_FOUND_MEMBER));
        member.changeRole(MemberRole.SELLER);
        return SellerDto.from(sellerRepository.save(Seller.from(sellerDto)));
    }

    // 제품 재고 변경
    @Transactional
    public ProductStockUpdateDto updateProductStock(TokenMemberInfo tokenMemberInfo, Long productId, ProductStockUpdateDto productStockUpdateDto) {

        validateProductId(productId, productStockUpdateDto.getProductId());
        validateMemberRole(tokenMemberInfo.getRole());

        eventProducerService.sendEvent("stock-update",
                UpdateStockDto.builder()
                        .productId(productId)
                        .count(productStockUpdateDto.getCount())
                        .build());

        return ProductStockUpdateDto.builder()
                .productId(productId)
                .name(productStockUpdateDto.getName())
                .count(productStockUpdateDto.getCount())
                .build();
    }

    private static void validateMemberRole(MemberRole memberRole) {
        if (!memberRole.equals(MemberRole.SELLER)) {
            throw new MemberException(ErrorCode.ROLE_NOT_ALLOWED);
        }
    }

    private static void validateProductId(Long productId, Long dtoToProductId) {
        if (!productId.equals(dtoToProductId)) {
            throw new MemberException(ErrorCode.MISS_MATCH_PRODUCT);
        }
    }

}
