package com.hoxkar.auth.controller;

import com.hoxkar.auth.pojo.dto.ChangePasswordRequest;
import com.hoxkar.auth.pojo.dto.LoginRequest;
import com.hoxkar.auth.pojo.vo.LoginResponse;
import com.hoxkar.auth.pojo.vo.UserInfoVO;
import com.hoxkar.auth.service.AuthService;
import com.hoxkar.common.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并获取访问令牌")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        return authService.login(loginRequest);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public ApiResponse<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        log.info("刷新令牌请求");
        return authService.refreshToken(refreshToken);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出并清除令牌")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        log.info("用户登出请求");
        return authService.logout(token);
    }
    
    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证访问令牌是否有效")
    public ApiResponse<Boolean> validateToken(@RequestParam String token) {
        log.info("验证令牌请求");
        return authService.validateToken(token);
    }
    
    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public ApiResponse<UserInfoVO> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        log.info("获取当前用户信息请求");
        return authService.getCurrentUser(token);
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    public ApiResponse<Void> changePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {
        String token = authorization.replace("Bearer ", "");
        log.info("修改密码请求");
        return authService.changePassword(token, request);
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "管理员重置用户密码")
    public ApiResponse<Void> resetPassword(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "租户ID") @RequestParam String tenantId) {
        log.info("重置密码请求: {}", username);
        return authService.resetPassword(username, tenantId);
    }
    
    @GetMapping("/check-username")
    @Operation(summary = "检查用户名是否存在", description = "检查指定租户下用户名是否已存在")
    public ApiResponse<Boolean> checkUsernameExists(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "租户ID") @RequestParam String tenantId) {
        log.info("检查用户名是否存在: {}", username);
        return authService.checkUsernameExists(username, tenantId);
    }
    
    @GetMapping("/check-email")
    @Operation(summary = "检查邮箱是否存在", description = "检查指定租户下邮箱是否已存在")
    public ApiResponse<Boolean> checkEmailExists(
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "租户ID") @RequestParam String tenantId) {
        log.info("检查邮箱是否存在: {}", email);
        return authService.checkEmailExists(email, tenantId);
    }
    
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "认证服务健康检查")
    public ApiResponse<String> health() {
        return ApiResponse.success("认证服务运行正常");
    }
} 