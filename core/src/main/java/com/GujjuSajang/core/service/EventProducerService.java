package com.GujjuSajang.core.service;

import com.GujjuSajang.core.config.KafkaProducerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventProducerService {

    private final KafkaProducerConfig kafkaProducerConfig;

    public void sendEventWithKey(String topic, String key, Object data) {
        KafkaTemplate<String, Object> kafkaTemplate = kafkaProducerConfig.kafkaTemplateWithTransactionId();
        kafkaTemplate.executeInTransaction(kt -> {
            kt.send(topic, key, data);
            return true;
        });
    }

    public void sendEvent(String topic, Object data) {
        KafkaTemplate<String, Object> kafkaTemplate = kafkaProducerConfig.kafkaTemplateWithTransactionId();
        kafkaTemplate.executeInTransaction(kt -> {
            kt.send(topic, data);  // key 없이 메시지 발송
            return true;
        });
    }

}
