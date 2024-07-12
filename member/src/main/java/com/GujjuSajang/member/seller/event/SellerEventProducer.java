package com.GujjuSajang.member.seller.event;

import com.GujjuSajang.core.dto.SetProductSalesStartTimeDto;
import com.GujjuSajang.core.dto.TokenMemberInfo;
import com.GujjuSajang.core.dto.UpdateStockDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import com.GujjuSajang.core.type.MemberRole;
import com.GujjuSajang.member.seller.dto.ProductStockUpdateDto;
import com.GujjuSajang.member.util.EventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SellerEventProducer {

    private final EventProducer eventProducer;

    // 제품 재고 변경
    @Transactional
    public ProductStockUpdateDto updateProductStock(TokenMemberInfo tokenMemberInfo, Long productId, ProductStockUpdateDto productStockUpdateDto) {

        validateProductId(productId, productStockUpdateDto.getProductId());
        validateMemberRole(tokenMemberInfo.getRole());

        eventProducer.sendEvent("stock-update",
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

    public void setProductSalesTime(TokenMemberInfo tokenMemberInfo, Long productId, LocalDateTime startTime) {
        validateMemberRole(tokenMemberInfo.getRole());

        eventProducer.sendEvent(
                "request-set-product-sales-time",
                SetProductSalesStartTimeDto.builder()
                        .memberId(tokenMemberInfo.getId())
                        .productId(productId)
                        .startTime(startTime)
                        .build());

    }
}
