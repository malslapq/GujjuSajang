package com.GujjuSajang.seller.service;

import com.GujjuSajang.Jwt.dto.TokenMemberInfo;
import com.GujjuSajang.exception.ErrorCode;
import com.GujjuSajang.exception.MemberException;
import com.GujjuSajang.exception.ProductException;
import com.GujjuSajang.member.entity.Member;
import com.GujjuSajang.member.repository.MemberRepository;
import com.GujjuSajang.member.type.MemberRole;
import com.GujjuSajang.seller.dto.ProductStockUpdateDto;
import com.GujjuSajang.seller.dto.SellerDto;
import com.GujjuSajang.seller.entity.Seller;
import com.GujjuSajang.seller.repository.SellerRepository;
import com.GujjuSajang.product.stock.Entity.Stock;
import com.GujjuSajang.product.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.GujjuSajang.member.service.MemberService.validateMemberId;

@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final StockRepository stockRepository;
    private final MemberRepository memberRepository;

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
    public ProductStockUpdateDto updateProductStock(Long sellerId, TokenMemberInfo tokenMemberInfo, Long productId, ProductStockUpdateDto productStockUpdateDto) {

        validateProductId(productId, productStockUpdateDto.getProductId());
        validateMemberRole(tokenMemberInfo.getRole());

        Stock stock = stockRepository.findByProductId(productId).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_STOCK));
        stock.updateCount(productStockUpdateDto.getCount());

        return ProductStockUpdateDto.builder()
                .stockId(stock.getProductId())
                .productId(productId)
                .name(productStockUpdateDto.getName())
                .count(stock.getCount())
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
