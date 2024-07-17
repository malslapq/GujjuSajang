package com.GujjuSajang.apigateway.config;

import com.GujjuSajang.apigateway.controller.AuthController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> route(AuthController authController) {
        return RouterFunctions
                .route()
                .POST("/refresh", authController::refreshAccessToken)
                .POST("/logout", authController::logout)
                .build();
    }
}
