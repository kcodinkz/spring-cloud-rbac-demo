package com.hoxkar.auth.service.impl;

import com.hoxkar.auth.entity.User;
import com.hoxkar.auth.pojo.dto.ChangePasswordRequest;
import com.hoxkar.auth.pojo.dto.LoginRequest;
import com.hoxkar.auth.pojo.vo.LoginResponse;
import com.hoxkar.auth.pojo.vo.UserInfoVO;
import com.hoxkar.auth.repository.UserRepository;
import com.hoxkar.auth.service.AuthService;
import com.hoxkar.common.exception.BusinessException;
import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.common.util.JwtTokenUtil;
import com.hoxkar.common.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

// Authentication Service Implementation
/**
 * 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public ApiResponse<LoginResponse> login(LoginRequest loginRequest) {
        try {
            // Set tenant context // 设置租户上下文
            TenantContext.setTenantId(loginRequest.getTenantId());
            
            // Find user // 查找用户
            Long tenantId = Long.valueOf(loginRequest.getTenantId());
            User user = userRepository.findByTenantIdAndUsername(tenantId, loginRequest.getUsername())
                    .orElseThrow(() -> BusinessException.of("Username or password is incorrect")); // 用户名或密码错误
            
            // Validate password // 验证密码
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw BusinessException.of("Username or password is incorrect"); // 用户名或密码错误
            }
            
            // Validate user status // 验证用户状态
            if (!"ACTIVE".equals(user.getStatus())) {
                throw BusinessException.of("User is disabled"); // 用户已被禁用
            }
            
            // Create authentication object // 创建认证对象
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword());
            
            // Authenticate // 进行认证
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate access token and refresh token // 生成访问令牌和刷新令牌
            String accessToken = jwtTokenUtil.generateToken(user, user.getTenantId().toString());
            String refreshToken = jwtTokenUtil.generateRefreshToken(user, user.getTenantId().toString());
            
            // Update last login time // 更新最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Store refresh token in Redis // 将刷新令牌存储到Redis
            jwtTokenUtil.storeRefreshToken(user.getUsername(), refreshToken);
            
            // Build response // 构建响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(accessToken);
            loginResponse.setRefreshToken(refreshToken);
            loginResponse.setTokenType("Bearer");
            loginResponse.setExpiresAt(LocalDateTime.now().plusHours(24));
            loginResponse.setUserId(user.getId());
            loginResponse.setUsername(user.getUsername());
            loginResponse.setTenantId(user.getTenantId().toString());
            loginResponse.setRealName(user.getRealName());
            loginResponse.setEmail(user.getEmail());
            loginResponse.setIsSuperAdmin(user.getIsSuperAdmin());
            
            log.info("User {} login successful", user.getUsername()); // 用户 {} 登录成功
            return ApiResponse.success("Login successful", loginResponse); // 登录成功
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Login failed: ", e); // 登录失败
            throw BusinessException.of("Login failed"); // 登录失败
        } finally {
            TenantContext.clear();
        }
    }
    
    @Override
    public ApiResponse<LoginResponse> refreshToken(String refreshToken) {
        try {
            // Validate refresh token // 验证刷新令牌
            if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
                throw BusinessException.of("Invalid refresh token"); // 无效的刷新令牌
            }
            
            // Extract user info from token // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(refreshToken);
            String tenantIdStr = jwtTokenUtil.extractTenantId(refreshToken);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // Find user // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> BusinessException.of("User does not exist")); // 用户不存在
            
            // Validate refresh token in Redis // 验证Redis中的刷新令牌
            if (!jwtTokenUtil.validateRefreshToken(username, refreshToken)) {
                throw BusinessException.of("Refresh token has expired"); // 刷新令牌已失效
            }
            
            // Generate new access token // 生成新的访问令牌
            String newAccessToken = jwtTokenUtil.generateToken(user, user.getTenantId().toString());
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(user, user.getTenantId().toString());
            
            // Update refresh token in Redis // 更新Redis中的刷新令牌
            jwtTokenUtil.storeRefreshToken(username, newRefreshToken);
            
            // Build response // 构建响应
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setAccessToken(newAccessToken);
            loginResponse.setRefreshToken(newRefreshToken);
            loginResponse.setTokenType("Bearer");
            loginResponse.setExpiresAt(LocalDateTime.now().plusHours(24));
            loginResponse.setUserId(user.getId());
            loginResponse.setUsername(user.getUsername());
            loginResponse.setTenantId(user.getTenantId().toString());
            loginResponse.setRealName(user.getRealName());
            loginResponse.setEmail(user.getEmail());
            loginResponse.setIsSuperAdmin(user.getIsSuperAdmin());
            
            return ApiResponse.success("Token refreshed successfully", loginResponse); // 令牌刷新成功
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed: ", e); // 令牌刷新失败
            throw BusinessException.of("Token refresh failed"); // 令牌刷新失败
        }
    }
    
    @Override
    public ApiResponse<Void> logout(String token) {
        try {
            // Extract user info from token // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(token);
            String tenantIdStr = jwtTokenUtil.extractTenantId(token);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // Find user // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> BusinessException.of("User does not exist")); // 用户不存在
            
            // Delete refresh token from Redis // 从Redis中删除刷新令牌
            jwtTokenUtil.deleteRefreshToken(username);
            
            // Add token to blacklist // 将令牌加入黑名单
            jwtTokenUtil.addToBlacklist(token);
            
            log.info("User {} logout successful", user.getUsername()); // 用户 {} 登出成功
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Logout failed: ", e); // 登出失败
            throw BusinessException.of("Logout failed"); // 登出失败
        }
    }
    
    @Override
    public ApiResponse<Boolean> validateToken(String token) {
        try {
            // Check if token is in blacklist // 检查令牌是否在黑名单中
            if (jwtTokenUtil.isTokenBlacklisted(token)) {
                return ApiResponse.success(false);
            }
            
            // Extract user info from token // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(token);
            String tenantIdStr = jwtTokenUtil.extractTenantId(token);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // Find user // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElse(null);
            
            if (user == null) {
                return ApiResponse.success(false);
            }
            
            // Validate token // 验证令牌
            boolean isValid = jwtTokenUtil.validateToken(token, user);
            return ApiResponse.success(isValid);
            
        } catch (Exception e) {
            log.error("Token validation failed: ", e); // 令牌验证失败
            return ApiResponse.success(false);
        }
    }
    
    @Override
    public ApiResponse<UserInfoVO> getCurrentUser(String token) {
        try {
            // Extract user info from token // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(token);
            String tenantIdStr = jwtTokenUtil.extractTenantId(token);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // Find user // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> BusinessException.of("User does not exist")); // 用户不存在
            
            // Build user info // 构建用户信息
            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setId(user.getId());
            userInfo.setTenantId(user.getTenantId().toString());
            userInfo.setUsername(user.getUsername());
            userInfo.setEmail(user.getEmail());
            userInfo.setPhone(user.getPhone());
            userInfo.setRealName(user.getRealName());
            userInfo.setStatus(user.getStatus());
            userInfo.setIsSuperAdmin(user.getIsSuperAdmin());
            userInfo.setLastLoginTime(user.getLastLoginAt());
            userInfo.setCreatedAt(user.getCreatedAt());
            userInfo.setUpdatedAt(user.getUpdatedAt());
            
            return ApiResponse.success(userInfo);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Get user info failed: ", e); // 获取用户信息失败
            throw BusinessException.of("Get user info failed"); // 获取用户信息失败
        }
    }
    
    @Override
    public ApiResponse<Void> changePassword(String token, ChangePasswordRequest request) {
        try {
            // Validate new password and confirm password are the same // 验证新密码和确认密码是否一致
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw BusinessException.of("New password and confirm password do not match"); // 新密码和确认密码不一致
            }
            
            // Extract user info from token // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(token);
            String tenantIdStr = jwtTokenUtil.extractTenantId(token);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // Find user // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> BusinessException.of("User does not exist")); // 用户不存在
            
            // Validate old password // 验证旧密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw BusinessException.of("Old password is incorrect"); // 旧密码错误
            }
            
            // Update password // 更新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Clear user's refresh token, force re-login // 清除用户的刷新令牌，强制重新登录
            jwtTokenUtil.deleteRefreshToken(username);
            
            log.info("User {} changed password successfully", username); // 用户 {} 修改密码成功
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Change password failed: ", e); // 修改密码失败
            throw BusinessException.of("Change password failed"); // 修改密码失败
        }
    }
    
    @Override
    public ApiResponse<Void> resetPassword(String username, String tenantId) {
        try {
            // Find user // 查找用户
            User user = userRepository.findByTenantIdAndUsername(Long.valueOf(tenantId), username)
                    .orElseThrow(() -> BusinessException.of("User does not exist")); // 用户不存在
            
            // Generate default password (can be modified as needed) // 生成默认密码（这里可以根据需求修改）
            String defaultPassword = "123456";
            user.setPassword(passwordEncoder.encode(defaultPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Clear user's refresh token // 清除用户的刷新令牌
            jwtTokenUtil.deleteRefreshToken(username);
            
            log.info("User {} password reset successfully", username); // 用户 {} 密码重置成功
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Reset password failed: ", e); // 重置密码失败
            throw BusinessException.of("Reset password failed"); // 重置密码失败
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkUsernameExists(String username, String tenantId) {
        try {
            boolean exists = userRepository.existsByTenantIdAndUsername(Long.valueOf(tenantId), username);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            log.error("Check if username exists failed: ", e); // 检查用户名是否存在失败
            throw BusinessException.of("Check if username exists failed"); // 检查用户名是否存在失败
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkEmailExists(String email, String tenantId) {
        try {
            boolean exists = userRepository.existsByTenantIdAndEmail(Long.valueOf(tenantId), email);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            log.error("Check if email exists failed: ", e); // 检查邮箱是否存在失败
            throw BusinessException.of("Check if email exists failed"); // 检查邮箱是否存在失败
        }
    }
} 