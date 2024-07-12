package com.GujjuSajang.product.event;


import com.GujjuSajang.core.dto.SetProductSalesStartTimeDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.MemberException;
import com.GujjuSajang.core.exception.ProductException;
import com.GujjuSajang.product.entity.Product;
import com.GujjuSajang.product.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    @KafkaListener(topics = {"request-set-product-sales-time"}, groupId = "product-service")
    public void validateSellerId(Message<?> message) {
        SetProductSalesStartTimeDto setProductSalesStartTimeDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
        });
        Product product = productRepository.findById(setProductSalesStartTimeDto.getProductId()).orElseThrow(() -> new ProductException(ErrorCode.NOT_FOUND_PRODUCT));

        if (!product.getSellerId().equals(setProductSalesStartTimeDto.getMemberId())) {
            throw new MemberException(ErrorCode.ROLE_NOT_ALLOWED);
        }

        eventProducer.sendEvent("success-validate-seller-id-from-set-sales-time", setProductSalesStartTimeDto);
    }

}
