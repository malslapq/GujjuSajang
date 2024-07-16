package com.GujjuSajang.webflux.controller;

import com.GujjuSajang.webflux.dto.StockDto;
import com.GujjuSajang.webflux.service.StreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@Tag(name = "실시간 API")
@RequestMapping("/stream")
@RestController
@RequiredArgsConstructor
public class StreamController {

    private final Sinks.Many<StockDto> stockSink;
    private final StreamService streamService;

    @Operation(summary = "실시간 재고 확인 API", description = "제품의 ID로 구독하고 구독된 클라이언트들에게 재고가 업데이트 될 때 재고 상태를 응답함")
    @GetMapping("/product/{product-id}/stock")
    public Flux<Object> getStock(@PathVariable("product-id") Long productId) {
        return Flux.merge(
                streamService.getStock(productId).flux().map(stockDto -> (Object) stockDto),
                stockSink.asFlux().filter(stockDto -> stockDto.getProductId().equals(productId)).map(stockDto -> (Object) stockDto),
                Flux.interval(Duration.ofSeconds(20)).map(tick -> (Object) "health check")
        );
    }

}
