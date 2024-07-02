package com.GujjuSajang.apigateway.handler;

import com.GujjuSajang.apigateway.exception.ApiGatewayException;
import com.GujjuSajang.apigateway.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.GujjuSajang.apigateway.filter.CustomRoutingFilter.MEMBER_SERVICE;
import static com.GujjuSajang.apigateway.filter.CustomRoutingFilter.OTHER_SERVICE;

@Component
@RequiredArgsConstructor
public class RequestHandlerFactory {

    private final MemberRequestHandler memberRequestHandler;
    private final OtherRequestHandler otherRequestHandler;

    public RequestHandler getRequestHandler(String serviceName) {
        return switch (serviceName) {
            case MEMBER_SERVICE -> memberRequestHandler;
            case OTHER_SERVICE -> otherRequestHandler;
            default -> throw new ApiGatewayException(ErrorCode.INVALID_SERVICE_URI);
        };
    }

}
