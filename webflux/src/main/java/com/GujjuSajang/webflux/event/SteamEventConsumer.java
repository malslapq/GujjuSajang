package com.GujjuSajang.webflux.event;

import com.GujjuSajang.webflux.dto.StockDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SteamEventConsumer {

    private final ObjectMapper objectMapper;
    private final Sinks.Many<StockDto> stockSink;

    @KafkaListener(topics = {"stream-stock"})
    public void listenStockUpdate(Message<?> message) {
        List<StockDto> stockDtos = objectMapper.convertValue(message.getPayload(), new TypeReference<>() {
        });
        for (StockDto stockDto : stockDtos) {
            stockSink.tryEmitNext(stockDto);
        }
    }
}
