package com.hoxkar.common.config;

import com.hoxkar.common.exception.BusinessException;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Feign错误解码器
 * 用于处理服务间调用的异常
 */
@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {
    
    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // 读取响应体
            String responseBody = "";
            if (response.body() != null) {
                try (InputStream inputStream = response.body().asInputStream()) {
                    responseBody = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
            
            log.error("Feign call failed - Method: {}, Status: {}, Response: {}", 
                    methodKey, response.status(), responseBody);
            
            // 根据HTTP状态码返回相应的异常
            switch (response.status()) {
                case 400:
                    return new BusinessException("Bad Request: " + responseBody);
                case 401:
                    return new BusinessException("Unauthorized: " + responseBody);
                case 403:
                    return new BusinessException("Forbidden: " + responseBody);
                case 404:
                    return new BusinessException("Service Not Found: " + responseBody);
                case 500:
                    return new BusinessException("Internal Server Error: " + responseBody);
                case 502:
                    return new BusinessException("Bad Gateway: " + responseBody);
                case 503:
                    return new BusinessException("Service Unavailable: " + responseBody);
                case 504:
                    return new BusinessException("Gateway Timeout: " + responseBody);
                default:
                    return new BusinessException("Service call failed: " + responseBody);
            }
            
        } catch (IOException e) {
            log.error("Failed to decode Feign error response", e);
            return new BusinessException("Service call failed");
        }
    }
} 