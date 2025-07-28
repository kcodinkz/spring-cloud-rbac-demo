package com.hoxkar.auth.client;

import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {
    
    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    ApiResponse<Object> getUserByUsername(@PathVariable String username);
    
    /**
     * 根据邮箱获取用户信息
     */
    @GetMapping("/email/{email}")
    ApiResponse<Object> getUserByEmail(@PathVariable String email);
    
    /**
     * 创建用户
     */
    @PostMapping
    ApiResponse<Object> createUser(@RequestBody Object request);
    
    /**
     * 更新用户
     */
    @PostMapping("/{userId}")
    ApiResponse<Object> updateUser(@PathVariable Long userId, @RequestBody Object request);
} 