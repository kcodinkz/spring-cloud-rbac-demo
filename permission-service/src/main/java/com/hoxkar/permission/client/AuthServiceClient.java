package com.hoxkar.permission.client;

import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 认证服务Feign客户端
 */
@FeignClient(name = "auth-service", path = "/api/auth")
public interface AuthServiceClient {
    
    /**
     * 验证用户令牌
     */
    @GetMapping("/validate-token")
    ApiResponse<Boolean> validateToken(@RequestHeader("Authorization") String token);
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    ApiResponse<Object> getCurrentUser(@RequestHeader("Authorization") String token);
} 