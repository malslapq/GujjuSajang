package com.GujjuSajang.product.config;

import com.GujjuSajang.product.stock.dto.StockDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SinkConfig {

    @Bean
    public Sinks.Many<StockDto> stockEventSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }

}
