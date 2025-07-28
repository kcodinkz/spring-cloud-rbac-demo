package com.hoxkar.auth.service;

import com.hoxkar.auth.pojo.dto.ChangePasswordRequest;
import com.hoxkar.auth.pojo.dto.LoginRequest;
import com.hoxkar.auth.pojo.vo.LoginResponse;
import com.hoxkar.auth.pojo.vo.UserInfoVO;
import com.hoxkar.common.pojo.vo.ApiResponse;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户登录
     */
    ApiResponse<LoginResponse> login(LoginRequest loginRequest);
    
    /**
     * 刷新令牌
     */
    ApiResponse<LoginResponse> refreshToken(String refreshToken);
    
    /**
     * 用户登出
     */
    ApiResponse<Void> logout(String token);
    
    /**
     * 验证令牌
     */
    ApiResponse<Boolean> validateToken(String token);
    
    /**
     * 获取当前用户信息
     */
    ApiResponse<UserInfoVO> getCurrentUser(String token);
    
    /**
     * 修改密码
     */
    ApiResponse<Void> changePassword(String token, ChangePasswordRequest request);
    
    /**
     * 重置密码
     */
    ApiResponse<Void> resetPassword(String username, String tenantId);
    
    /**
     * 检查用户名是否存在
     */
    ApiResponse<Boolean> checkUsernameExists(String username, String tenantId);
    
    /**
     * 检查邮箱是否存在
     */
    ApiResponse<Boolean> checkEmailExists(String email, String tenantId);
} 