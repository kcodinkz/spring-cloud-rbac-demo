package com.hoxkar.user.client;

import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 租户服务Feign客户端
 */
@FeignClient(name = "tenant-service", path = "/api/tenants")
public interface TenantServiceClient {
    
    /**
     * 根据租户代码获取租户信息
     */
    @GetMapping("/code/{tenantCode}")
    ApiResponse<Object> getTenantByCode(@PathVariable String tenantCode);
    
    /**
     * 更新租户用户数
     */
    @PutMapping("/{tenantId}/user-count")
    ApiResponse<Void> updateTenantUserCount(@PathVariable String tenantId, @RequestParam Integer userCount);
    
    /**
     * 检查租户是否存在
     */
    @GetMapping("/check-tenant-code/{tenantCode}")
    ApiResponse<Boolean> checkTenantCodeExists(@PathVariable String tenantCode);
} 