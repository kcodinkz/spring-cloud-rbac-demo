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

// Authentication Controller
/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "User authentication related APIs") // 认证管理，用户认证相关接口
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    @Operation(summary = "User Login", description = "User login and get access token") // 用户登录，用户登录并获取访问令牌
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("User login request: {}", loginRequest.getUsername()); // 用户登录请求
        return authService.login(loginRequest);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Use refresh token to get new access token") // 刷新令牌，使用刷新令牌获取新的访问令牌
    public ApiResponse<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        log.info("Refresh token request"); // 刷新令牌请求
        return authService.refreshToken(refreshToken);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "User logout and clear token") // 用户登出，用户登出并清除令牌
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        log.info("User logout request"); // 用户登出请求
        return authService.logout(token);
    }
    
    @GetMapping("/validate")
    @Operation(summary = "Validate Token", description = "Validate if access token is valid") // 验证令牌，验证访问令牌是否有效
    public ApiResponse<Boolean> validateToken(@RequestParam String token) {
        log.info("Validate token request"); // 验证令牌请求
        return authService.validateToken(token);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get Current User Info", description = "Get detailed info of current logged-in user") // 获取当前用户信息，获取当前登录用户的详细信息
    public ApiResponse<UserInfoVO> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        log.info("Get current user info request"); // 获取当前用户信息请求
        return authService.getCurrentUser(token);
    }
    
    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "User changes own password") // 修改密码，用户修改自己的密码
    public ApiResponse<Void> changePassword(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody ChangePasswordRequest request) {
        String token = authorization.replace("Bearer ", "");
        log.info("Change password request"); // 修改密码请求
        return authService.changePassword(token, request);
    }
    
    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password", description = "Admin resets user password") // 重置密码，管理员重置用户密码
    public ApiResponse<Void> resetPassword(
            @Parameter(description = "Username") @RequestParam String username, // 用户名
            @Parameter(description = "Tenant ID") @RequestParam String tenantId) { // 租户ID
        log.info("Reset password request: {}", username); // 重置密码请求
        return authService.resetPassword(username, tenantId);
    }
    
    @GetMapping("/check-username")
    @Operation(summary = "Check Username Exists", description = "Check if username exists under specified tenant") // 检查用户名是否存在，检查指定租户下用户名是否已存在
    public ApiResponse<Boolean> checkUsernameExists(
            @Parameter(description = "Username") @RequestParam String username, // 用户名
            @Parameter(description = "Tenant ID") @RequestParam String tenantId) { // 租户ID
        log.info("Check if username exists: {}", username); // 检查用户名是否存在
        return authService.checkUsernameExists(username, tenantId);
    }
    
    @GetMapping("/check-email")
    @Operation(summary = "Check Email Exists", description = "Check if email exists under specified tenant") // 检查邮箱是否存在，检查指定租户下邮箱是否已存在
    public ApiResponse<Boolean> checkEmailExists(
            @Parameter(description = "Email") @RequestParam String email, // 邮箱
            @Parameter(description = "Tenant ID") @RequestParam String tenantId) { // 租户ID
        log.info("Check if email exists: {}", email); // 检查邮箱是否存在
        return authService.checkEmailExists(email, tenantId);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Authentication service health check") // 健康检查，认证服务健康检查
    public ApiResponse<String> health() {
        return ApiResponse.success("Authentication service is running normally"); // 认证服务运行正常
    }
} 