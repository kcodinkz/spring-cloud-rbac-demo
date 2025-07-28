package com.hoxkar.common.client;

import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 权限服务Feign客户端
 * 用于权限验证切面调用权限服务
 */
@FeignClient(name = "permission-service", path = "/api/permissions")
public interface PermissionServiceClient {
    
    /**
     * 检查用户是否有指定权限
     */
    @GetMapping("/users/{userId}/check-permission/{permissionCode}")
    ApiResponse<Boolean> checkUserPermission(@PathVariable Long userId, @PathVariable String permissionCode);
    
    /**
     * 检查用户是否有指定角色
     */
    @GetMapping("/users/{userId}/check-role/{roleCode}")
    ApiResponse<Boolean> checkUserRole(@PathVariable Long userId, @PathVariable String roleCode);
    
    /**
     * 获取用户的权限列表
     */
    @GetMapping("/users/{userId}/permissions")
    ApiResponse<List<Object>> getUserPermissions(@PathVariable Long userId);
    
    /**
     * 获取用户的角色列表
     */
    @GetMapping("/users/{userId}/roles")
    ApiResponse<List<Object>> getUserRoles(@PathVariable Long userId);
} 