package com.hoxkar.tenant.client;

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
     * 获取租户的用户数量
     */
    @GetMapping("/count/tenant/{tenantId}")
    ApiResponse<Long> getUserCountByTenant(@PathVariable String tenantId);
    
    /**
     * 获取租户的用户列表
     */
    @GetMapping("/tenant/{tenantId}")
    ApiResponse<Object> getUsersByTenant(@PathVariable String tenantId);
} 