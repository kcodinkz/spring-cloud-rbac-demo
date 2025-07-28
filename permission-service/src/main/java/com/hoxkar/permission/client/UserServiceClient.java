package com.hoxkar.permission.client;

import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {
    
    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{userId}")
    ApiResponse<Object> getUserById(@PathVariable Long userId);
    
    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    ApiResponse<Object> getUserByUsername(@PathVariable String username);
    
    /**
     * 检查用户是否存在
     */
    @GetMapping("/exists/{userId}")
    ApiResponse<Boolean> checkUserExists(@PathVariable Long userId);
} 