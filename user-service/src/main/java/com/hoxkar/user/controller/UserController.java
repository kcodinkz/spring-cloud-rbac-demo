package com.hoxkar.user.controller;

import com.hoxkar.user.pojo.dto.ChangePasswordRequest;
import com.hoxkar.user.pojo.dto.CreateUserRequest;
import com.hoxkar.user.pojo.dto.UpdateUserRequest;
import com.hoxkar.user.pojo.dto.UserSearchRequest;
import com.hoxkar.user.pojo.vo.UserStatisticsVO;
import com.hoxkar.user.pojo.vo.UserVO;
import com.hoxkar.user.service.UserService;
import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.common.pojo.dto.PageRequestDTO;
import com.hoxkar.common.pojo.vo.PageResponseVO;
import com.hoxkar.common.util.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户管理相关接口")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public ApiResponse<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("创建用户请求: {}", request.getUsername());
        return userService.createUser(request);
    }
    
    @PutMapping("/{userId}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public ApiResponse<UserVO> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        log.info("更新用户请求: {}", userId);
        return userService.updateUser(userId, request);
    }
    
    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "删除指定用户")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        log.info("删除用户请求: {}", userId);
        return userService.deleteUser(userId);
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ApiResponse<UserVO> getUserById(@PathVariable Long userId) {
        log.info("获取用户详情请求: {}", userId);
        return userService.getUserById(userId);
    }
    
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名获取用户信息")
    public ApiResponse<UserVO> getUserByUsername(@PathVariable String username) {
        log.info("根据用户名获取用户请求: {}", username);
        return userService.getUserByUsername(username);
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "根据邮箱获取用户", description = "根据邮箱获取用户信息")
    public ApiResponse<UserVO> getUserByEmail(@PathVariable String email) {
        log.info("根据邮箱获取用户请求: {}", email);
        return userService.getUserByEmail(email);
    }
    
    @GetMapping
    @Operation(summary = "获取用户列表", description = "获取所有用户列表")
    public ApiResponse<List<UserVO>> getAllUsers() {
        log.info("获取用户列表请求");
        return userService.getAllUsers();
    }
    
    @PostMapping("/page")
    @Operation(summary = "分页获取用户", description = "分页获取用户列表")
    public ApiResponse<PageResponseVO<UserVO>> getUsersByPage(@Valid @RequestBody PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageUtils.toPageable(pageRequestDTO);
        Page<UserVO> page = userService.getUsersByPage(pageable).getData();
        PageResponseVO<UserVO> vo = PageUtils.toPageResponseVO(page);
        return ApiResponse.success(vo);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取用户", description = "根据用户状态获取用户列表")
    public ApiResponse<List<UserVO>> getUsersByStatus(@PathVariable String status) {
        log.info("根据状态获取用户请求: {}", status);
        return userService.getUsersByStatus(status);
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据真实姓名搜索用户")
    public ApiResponse<List<UserVO>> searchUsersByRealName(@RequestParam String realName) {
        log.info("搜索用户请求: {}", realName);
        return userService.searchUsersByRealName(realName);
    }
    
    @PostMapping("/search/advanced")
    @Operation(summary = "高级搜索用户", description = "根据多个条件高级搜索用户")
    public ApiResponse<Page<UserVO>> searchUsers(@Valid @RequestBody UserSearchRequest request) {
        log.info("高级搜索用户请求: {}", request);
        return userService.searchUsers(request);
    }
    
    @GetMapping("/super-admin/{isSuperAdmin}")
    @Operation(summary = "根据超级管理员状态获取用户", description = "根据是否为超级管理员获取用户列表")
    public ApiResponse<List<UserVO>> getUsersBySuperAdmin(@PathVariable Boolean isSuperAdmin) {
        log.info("根据超级管理员状态获取用户请求: {}", isSuperAdmin);
        return userService.getUsersBySuperAdmin(isSuperAdmin);
    }
    
    @PostMapping("/{userId}/change-password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public ApiResponse<Void> changePassword(@PathVariable Long userId, @Valid @RequestBody ChangePasswordRequest request) {
        log.info("修改密码请求: {}", userId);
        return userService.changePassword(userId, request);
    }
    
    @PostMapping("/{userId}/enable")
    @Operation(summary = "启用用户", description = "启用指定用户")
    public ApiResponse<Void> enableUser(@PathVariable Long userId) {
        log.info("启用用户请求: {}", userId);
        return userService.enableUser(userId);
    }
    
    @PostMapping("/{userId}/disable")
    @Operation(summary = "禁用用户", description = "禁用指定用户")
    public ApiResponse<Void> disableUser(@PathVariable Long userId) {
        log.info("禁用用户请求: {}", userId);
        return userService.disableUser(userId);
    }
    
    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "重置密码", description = "重置用户密码")
    public ApiResponse<Void> resetPassword(@PathVariable Long userId, @RequestParam String newPassword) {
        log.info("重置密码请求: {}", userId);
        return userService.resetPassword(userId, newPassword);
    }
    
    @PostMapping("/batch/enable")
    @Operation(summary = "批量启用用户", description = "批量启用多个用户")
    public ApiResponse<Void> batchEnableUsers(@RequestBody List<Long> userIds) {
        log.info("批量启用用户请求: {}", userIds);
        return userService.batchEnableUsers(userIds);
    }
    
    @PostMapping("/batch/disable")
    @Operation(summary = "批量禁用用户", description = "批量禁用多个用户")
    public ApiResponse<Void> batchDisableUsers(@RequestBody List<Long> userIds) {
        log.info("批量禁用用户请求: {}", userIds);
        return userService.batchDisableUsers(userIds);
    }
    
    @PostMapping("/batch/delete")
    @Operation(summary = "批量删除用户", description = "批量删除多个用户")
    public ApiResponse<Void> batchDeleteUsers(@RequestBody List<Long> userIds) {
        log.info("批量删除用户请求: {}", userIds);
        return userService.batchDeleteUsers(userIds);
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取用户统计信息", description = "获取用户相关的统计信息")
    public ApiResponse<UserStatisticsVO> getUserStatistics() {
        log.info("获取用户统计信息请求");
        return userService.getUserStatistics();
    }
    
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查指定用户名是否已存在")
    public ApiResponse<Boolean> checkUsernameExists(@RequestParam String username) {
        log.info("检查用户名是否存在请求: {}", username);
        return userService.checkUsernameExists(username);
    }
    
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱是否存在", description = "检查指定邮箱是否已存在")
    public ApiResponse<Boolean> checkEmailExists(@RequestParam String email) {
        log.info("检查邮箱是否存在请求: {}", email);
        return userService.checkEmailExists(email);
    }
    
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "用户服务健康检查")
    public ApiResponse<String> health() {
        return ApiResponse.success("用户服务运行正常");
    }
} 