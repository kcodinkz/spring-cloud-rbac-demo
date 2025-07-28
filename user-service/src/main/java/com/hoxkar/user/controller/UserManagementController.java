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

/**
 * 用户管理控制器
 * 演示权限注解的使用
 */
@Slf4j
@RestController
@RequestMapping("/api/user-management")
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserManagementController {
    
    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "需要用户查看权限")
    @RequiresPermission("user:view")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> getUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "用户列表获取成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("获取用户列表");
        return ApiResponse.success(result);
    }
    
    @PostMapping("/users")
    @Operation(summary = "创建用户", description = "需要用户创建权限")
    @RequiresPermission("user:create")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> createUser(@RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "用户创建成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("创建用户");
        return ApiResponse.success(result);
    }
    
    @PutMapping("/users/{userId}")
    @Operation(summary = "更新用户", description = "需要用户编辑权限")
    @RequiresPermission("user:edit")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> updateUser(@PathVariable Long userId, @RequestBody Object request) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "用户更新成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("更新用户: {}", userId);
        return ApiResponse.success(result);
    }
    
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "删除用户", description = "需要用户删除权限")
    @RequiresPermission("user:delete")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "用户删除成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("删除用户: {}", userId);
        return ApiResponse.success(result);
    }
    
    @GetMapping("/users/batch")
    @Operation(summary = "批量操作用户", description = "需要批量操作权限")
    @RequiresPermission(value = "user:batch", logic = RequiresPermission.LogicType.AND)
    @RequiresTenant
    public ApiResponse<Map<String, Object>> batchOperation() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "批量操作成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("批量操作用户");
        return ApiResponse.success(result);
    }
    
    @GetMapping("/admin/users")
    @Operation(summary = "管理员查看用户", description = "需要管理员角色")
    @RequiresRole("ADMIN")
    @RequiresTenant
    public ApiResponse<Map<String, Object>> adminViewUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "管理员查看用户成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("管理员查看用户");
        return ApiResponse.success(result);
    }
    
    @GetMapping("/super-admin/users")
    @Operation(summary = "超级管理员查看用户", description = "需要超级管理员角色")
    @RequiresRole(value = "SUPER_ADMIN", logic = RequiresRole.LogicType.OR)
    @RequiresTenant(required = false)
    public ApiResponse<Map<String, Object>> superAdminViewUsers() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "超级管理员查看用户成功");
        result.put("timestamp", System.currentTimeMillis());
        
        log.info("超级管理员查看用户");
        return ApiResponse.success(result);
    }
} 