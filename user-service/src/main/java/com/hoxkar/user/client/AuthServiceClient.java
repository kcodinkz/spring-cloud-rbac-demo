package com.hoxkar.user.client;

import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    
    /**
     * 检查用户名是否存在
     */
    @GetMapping("/check-username/{username}")
    ApiResponse<Boolean> checkUsernameExists(@PathVariable String username);
    
    /**
     * 检查邮箱是否存在
     */
    @GetMapping("/check-email/{email}")
    ApiResponse<Boolean> checkEmailExists(@PathVariable String email);
} 