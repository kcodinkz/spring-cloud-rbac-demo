package com.hoxkar.user.client;

import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 权限服务Feign客户端
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
    
    /**
     * 为用户分配角色
     */
    @PostMapping("/users/assign-roles")
    ApiResponse<Void> assignRolesToUser(@RequestBody Object request);
} 