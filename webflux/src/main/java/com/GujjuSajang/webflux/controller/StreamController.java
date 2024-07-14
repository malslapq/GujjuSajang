package com.GujjuSajang.webflux.controller;

import com.GujjuSajang.webflux.dto.StockDto;
import com.GujjuSajang.webflux.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RequestMapping("/stream")
@RestController
@RequiredArgsConstructor
public class StreamController {

    private final Sinks.Many<StockDto> stockSink;
    private final StreamService streamService;

    @GetMapping("/product/{product-id}/stock")
    public Flux<Object> getStock(@PathVariable("product-id") Long productId) {
        return Flux.merge(
                streamService.getStock(productId).flux().map(stockDto -> (Object) stockDto),
                stockSink.asFlux().filter(stockDto -> stockDto.getProductId().equals(productId)).map(stockDto -> (Object) stockDto),
                Flux.interval(Duration.ofSeconds(20)).map(tick -> (Object) "health check")
        );
    }

}
