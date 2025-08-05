package com.hoxkar.user.controller;

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

// User Management Controller
/**
 * 用户管理控制器
 * 演示权限注解的使用
 */
@Slf4j
@RestController
@RequestMapping("/api/user-management")
@Tag(name = "User Management", description = "User management related APIs") // 用户管理相关接口
public class UserManagementController {
    
    @GetMapping("/users")
    @Operation(summary = "Get User List", description = "Requires user view permission") // 获取用户列表，需要用户查看权限
    @RequiresPermission("user:view")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> getUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "User list retrieved successfully"); // 用户列表获取成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Get user list"); // 获取用户列表
        return ApiResponse.success(result);
    }
    
    @PostMapping("/users")
    @Operation(summary = "Create User", description = "Requires user create permission") // 创建用户，需要用户创建权限
    @RequiresPermission("user:create")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> createUser(@RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "User created successfully"); // 用户创建成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Create user"); // 创建用户
        return ApiResponse.success(result);
    }
    
    @PutMapping("/users/{userId}")
    @Operation(summary = "Update User", description = "Requires user edit permission") // 更新用户，需要用户编辑权限
    @RequiresPermission("user:edit")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable Long userId, @RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "User updated successfully"); // 用户更新成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Update user: {}", userId); // 更新用户
        return ApiResponse.success(result);
    }
    
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete User", description = "Requires user delete permission") // 删除用户，需要用户删除权限
    @RequiresPermission("user:delete")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "User deleted successfully"); // 用户删除成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Delete user: {}", userId); // 删除用户
        return ApiResponse.success(result);
    }
    
    @GetMapping("/users/batch")
    @Operation(summary = "Batch Operate Users", description = "Requires batch operation permission") // 批量操作用户，需要批量操作权限
    @RequiresPermission(value = "user:batch", logic = RequiresPermission.LogicType.AND)
    @RequiresTenant
    public ApiResponse<Map<String, Object>> batchOperation() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Batch operation successful"); // 批量操作成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Batch operate users"); // 批量操作用户
        return ApiResponse.success(result);
    }
    
    @GetMapping("/admin/users")
    @Operation(summary = "Admin View Users", description = "Requires admin role") // 管理员查看用户，需要管理员角色
    @RequiresRole("ADMIN")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> adminViewUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Admin viewed users successfully"); // 管理员查看用户成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Admin view users"); // 管理员查看用户
        return ApiResponse.success(result);
    }
    
    @GetMapping("/super-admin/users")
    @Operation(summary = "Super Admin View Users", description = "Requires super admin role") // 超级管理员查看用户，需要超级管理员角色
    @RequiresRole(value = "SUPER_ADMIN", logic = RequiresRole.LogicType.OR)
    @RequiresTenant(required = false)
    public ApiResponse<Map<String, Object>> superAdminViewUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Super admin viewed users successfully"); // 超级管理员查看用户成功
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("Super admin view users"); // 超级管理员查看用户
        return ApiResponse.success(result);
    }
} 