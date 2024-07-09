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

    @KafkaListener(topics = {"success-create-orders"}, groupId = "payment-service")
    public void createPayment(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            if (random.nextInt(5) < 1) {
                throw new RuntimeException("고객 귀책 이탈 시뮬레이션 20% 당첨");
            }

            int amount = createOrderEventDto.getCartProductsDtos().stream().mapToInt(cartProductsDto -> cartProductsDto.getPrice() * cartProductsDto.getCount()).sum();

            paymentRepository.save(Payment.builder()
                    .memberId(createOrderEventDto.getMemberId())
                    .ordersId(createOrderEventDto.getOrderId())
                    .status(PaymentStatus.COMPLETED)
                    .amount(amount)
                    .build());
            eventProducer.sendEvent("success-payment", createOrderEventDto);
        } catch (Exception e) {
            eventProducer.sendEvent("fail-payment", createOrderEventDto);
        }
    }

    @KafkaListener(topics = {"fail-payment"}, groupId = "payment-service")
    public void failPayment(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            int amount = createOrderEventDto.getCartProductsDtos().stream().mapToInt(cartProductsDto -> cartProductsDto.getPrice() * cartProductsDto.getCount()).sum();

            paymentRepository.save(Payment.builder()
                    .memberId(createOrderEventDto.getMemberId())
                    .ordersId(createOrderEventDto.getOrderId())
                    .status(PaymentStatus.FAILED)
                    .amount(amount)
                    .build());

        } catch (Exception e) {
            log.error("error create failed payment event message : {}", createOrderEventDto, e);
        }
    }

    @KafkaListener(topics = {"fail-create-orders-product"}, groupId = "payment-service")
    public void returnPaymentFromOrdersProducts(Message<?> message) {
        CreateOrderEventDto createOrderEventDto = null;
        try {
            createOrderEventDto = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
            });

            Payment payment = paymentRepository.findByOrdersId(createOrderEventDto.getOrderId()).orElseThrow(() -> new PaymentException(ErrorCode.NOT_FOUND_PAYMENT));
            payment.cancelPayment();
            paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("error create failed payment event message : {}", createOrderEventDto, e);
        }
    }



}
