package com.hoxkar.auth.client;

import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
     * 根据域名获取租户信息
     */
    @GetMapping("/domain/{domain}")
    ApiResponse<Object> getTenantByDomain(@PathVariable String domain);
    
    /**
     * 检查租户是否存在
     */
    @GetMapping("/check-tenant-code/{tenantCode}")
    ApiResponse<Boolean> checkTenantCodeExists(@PathVariable String tenantCode);
} 