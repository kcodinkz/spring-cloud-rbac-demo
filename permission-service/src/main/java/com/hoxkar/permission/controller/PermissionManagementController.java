package com.hoxkar.permission.controller;

import com.hoxkar.common.annotation.RequiresPermission;
import com.hoxkar.common.annotation.RequiresRole;
import com.hoxkar.common.annotation.RequiresTenant;
import com.hoxkar.common.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限管理控制器
 * 演示权限注解的使用
 */
@Slf4j
@RestController
@RequestMapping("/api/permission-management")
@Tag(name = "权限管理", description = "权限管理相关接口")
public class PermissionManagementController {
    
    @GetMapping("/roles")
    @Operation(summary = "获取角色列表", description = "需要角色查看权限")
    @RequiresPermission(value = "role:view")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> getRoles() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "角色列表获取成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("获取角色列表");
        return ApiResponse.success(result);
    }
    
    @PostMapping("/roles")
    @Operation(summary = "创建角色", description = "需要角色创建权限")
    @RequiresPermission(value = "role:create")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "角色创建成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("创建角色");
        return ApiResponse.success(result);
    }
    
    @GetMapping("/permissions")
    @Operation(summary = "获取权限列表", description = "需要权限查看权限")
    @RequiresPermission(value = "permission:view")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> getPermissions() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "权限列表获取成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("获取权限列表");
        return ApiResponse.success(result);
    }
    
    @PostMapping("/permissions")
    @Operation(summary = "创建权限", description = "需要权限创建权限")
    @RequiresPermission(value = "permission:create")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> createPermission(@RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "权限创建成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("创建权限");
        return ApiResponse.success(result);
    }
    
    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "为角色分配权限", description = "需要角色权限分配权限")
    @RequiresPermission(value = "role:edit")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> assignPermissionsToRole(@PathVariable Long roleId, @RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "权限分配成功");
        result.put("timestamp", System.currentTimeMillis());
        log.info("为角色分配权限: {}", roleId);
        return ApiResponse.success(result);
    }
    
    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "为用户分配角色", description = "需要用户角色分配权限")
    @RequiresPermission(value = "user:edit")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> assignRolesToUser(@PathVariable Long userId, @RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "角色分配成功");
        result.put("timestamp", System.currentTimeMillis());
        log.info("为用户分配角色: {}", userId);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/admin/permissions")
    @Operation(summary = "管理员查看权限", description = "需要管理员角色")
    @RequiresRole("ADMIN")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> adminViewPermissions() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "管理员查看权限成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("管理员查看权限");
        return ApiResponse.success(result);
    }
    
    @GetMapping("/system/permissions")
    @Operation(summary = "系统权限管理", description = "需要系统管理员角色")
    @RequiresRole(value = "", roles = {"SYSTEM_ADMIN", "SUPER_ADMIN"}, logic = RequiresRole.LogicType.OR)
    @RequiresTenant(required = false)
    public ApiResponse<Map<String, Object>> systemPermissionManagement() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "系统权限管理成功");
        result.put("timestamp", System.currentTimeMillis());
        log.info("系统权限管理");
        return ApiResponse.success(result);
    }
} 