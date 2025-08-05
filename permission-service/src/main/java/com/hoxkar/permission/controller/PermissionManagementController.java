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

// Permission Management Controller
/**
 * 权限管理控制器
 * 演示权限注解的使用
 */
@Slf4j
@RestController
@RequestMapping("/api/permission-management")
@Tag(name = "Permission Management", description = "Permission management related APIs") // 权限管理相关接口
public class PermissionManagementController {
    
    @GetMapping("/roles")
    @Operation(summary = "Get Role List", description = "Requires role view permission") // 获取角色列表，需要角色查看权限
    @RequiresPermission(value = "role:view")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> getRoles() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Role list retrieved successfully"); // 角色列表获取成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Get role list"); // 获取角色列表
        return ApiResponse.success(result);
    }
    
    @PostMapping("/roles")
    @Operation(summary = "Create Role", description = "Requires role create permission") // 创建角色，需要角色创建权限
    @RequiresPermission(value = "role:create")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> createRole(@RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Role created successfully"); // 角色创建成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Create role"); // 创建角色
        return ApiResponse.success(result);
    }
    
    @GetMapping("/permissions")
    @Operation(summary = "Get Permission List", description = "Requires permission view permission") // 获取权限列表，需要权限查看权限
    @RequiresPermission(value = "permission:view")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> getPermissions() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Permission list retrieved successfully"); // 权限列表获取成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Get permission list"); // 获取权限列表
        return ApiResponse.success(result);
    }
    
    @PostMapping("/permissions")
    @Operation(summary = "Create Permission", description = "Requires permission create permission") // 创建权限，需要权限创建权限
    @RequiresPermission(value = "permission:create")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> createPermission(@RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Permission created successfully"); // 权限创建成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Create permission"); // 创建权限
        return ApiResponse.success(result);
    }
    
    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Assign Permissions to Role", description = "Requires role permission assignment permission") // 为角色分配权限，需要角色权限分配权限
    @RequiresPermission(value = "role:edit")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> assignPermissionsToRole(@PathVariable Long roleId, @RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Permission assignment successful"); // 权限分配成功
        result.put("timestamp", System.currentTimeMillis());
        log.info("Assign permissions to role: {}", roleId); // 为角色分配权限
        return ApiResponse.success(result);
    }
    
    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "Assign Roles to User", description = "Requires user role assignment permission") // 为用户分配角色，需要用户角色分配权限
    @RequiresPermission(value = "user:edit")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> assignRolesToUser(@PathVariable Long userId, @RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Role assignment successful"); // 角色分配成功
        result.put("timestamp", System.currentTimeMillis());
        log.info("Assign roles to user: {}", userId); // 为用户分配角色
        return ApiResponse.success(result);
    }
    
    @GetMapping("/admin/permissions")
    @Operation(summary = "Admin View Permissions", description = "Requires admin role") // 管理员查看权限，需要管理员角色
    @RequiresRole("ADMIN")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> adminViewPermissions() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Admin viewed permissions successfully"); // 管理员查看权限成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Admin view permissions"); // 管理员查看权限
        return ApiResponse.success(result);
    }
    
    @GetMapping("/system/permissions")
    @Operation(summary = "System Permission Management", description = "Requires system admin or super admin role") // 系统权限管理，需要系统管理员或超级管理员角色
    @RequiresRole(value = "", roles = {"SYSTEM_ADMIN", "SUPER_ADMIN"}, logic = RequiresRole.LogicType.OR)
    @RequiresTenant(required = false)
    public ApiResponse<Map<String, Object>> systemPermissionManagement() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "System permission management successful"); // 系统权限管理成功
        result.put("timestamp", System.currentTimeMillis());
        log.info("System permission management"); // 系统权限管理
        return ApiResponse.success(result);
    }
} 