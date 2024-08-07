package com.GujjuSajang.payment.event;

import com.GujjuSajang.core.dto.CreateOrderEventDto;
import com.GujjuSajang.core.exception.ErrorCode;
import com.GujjuSajang.core.exception.PaymentException;
import com.GujjuSajang.payment.entity.Payment;
import com.GujjuSajang.payment.repository.PaymentRepository;
import com.GujjuSajang.payment.type.PaymentStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final EventProducer eventProducer;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    @KafkaListener(topics = {"succeed-check-stock"})
    public void paymentScreen(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            if (random.nextInt(5) < 1) {
                throw new RuntimeException("고객 변심 이탈");
            }

            eventProducer.sendEvent("request-payment", createOrderEventDto);
        } catch (Exception e) {
            log.error("failed to request payment");
            eventProducer.sendEvent("failed-request-payment", createOrderEventDto);
        }
    }

    @KafkaListener(topics = {"request-payment"})
    public void createPayment(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            if (random.nextInt(5) < 1) {
                throw new RuntimeException("결제 실패");
            }

            int amount = createOrderEventDto.getCartProductsDtos().stream().mapToInt(cartProductsDto -> cartProductsDto.getPrice() * cartProductsDto.getCount()).sum();

            Payment payment = paymentRepository.save(Payment.builder()
                    .memberId(createOrderEventDto.getMemberId())
                    .status(PaymentStatus.COMPLETED)
                    .amount(amount)
                    .build());

            createOrderEventDto.setPaymentId(payment.getId());

            eventProducer.sendEvent("success-payment", createOrderEventDto);
        } catch (Exception e) {
            log.error("failed to payment");
            eventProducer.sendEvent("failed-payment", createOrderEventDto);
        }
    }

    // 결제 성공 이후 취소 처리
    @KafkaListener(topics = {"failed-create-orders", "failed-create-orders-product", "failed-deduct-stock"})
    public void cancelPayment(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            Payment payment = paymentRepository.findById(createOrderEventDto.getPaymentId()).orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND_PAYMENT));
            payment.cancelPayment();
            paymentRepository.save(payment);

            eventProducer.sendEvent("cancel-payment", createOrderEventDto);

        } catch (Exception e) {
            log.error("error create failed payment event message : {}", createOrderEventDto, e);
        }
    }

}
