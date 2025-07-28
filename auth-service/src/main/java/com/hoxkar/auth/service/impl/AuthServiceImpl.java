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
            // 设置租户上下文
            TenantContext.setTenantId(loginRequest.getTenantId());
            
            // 查找用户
            Long tenantId = Long.valueOf(loginRequest.getTenantId());
            User user = userRepository.findByTenantIdAndUsername(tenantId, loginRequest.getUsername())
                    .orElseThrow(() -> BusinessException.of("用户名或密码错误"));
            
            // 验证密码
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw BusinessException.of("用户名或密码错误");
            }
            
            // 验证用户状态
            if (!"ACTIVE".equals(user.getStatus())) {
                throw BusinessException.of("用户已被禁用");
            }
            
            // 创建认证对象
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword());
            
            // 进行认证
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 生成访问令牌和刷新令牌
            String accessToken = jwtTokenUtil.generateToken(user, user.getTenantId().toString());
            String refreshToken = jwtTokenUtil.generateRefreshToken(user, user.getTenantId().toString());
            
            // 更新最后登录时间
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            // 将刷新令牌存储到Redis
            jwtTokenUtil.storeRefreshToken(user.getUsername(), refreshToken);
            
            // 构建响应
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
            
            log.info("用户 {} 登录成功", user.getUsername());
            return ApiResponse.success("登录成功", loginResponse);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录失败: ", e);
            throw BusinessException.of("登录失败");
        } finally {
            TenantContext.clear();
        }
    }
    
    @Override
    public ApiResponse<LoginResponse> refreshToken(String refreshToken) {
        try {
            // 验证刷新令牌
            if (!jwtTokenUtil.isRefreshToken(refreshToken)) {
                throw BusinessException.of("无效的刷新令牌");
            }
            
            // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(refreshToken);
            String tenantIdStr = jwtTokenUtil.extractTenantId(refreshToken);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证Redis中的刷新令牌
            if (!jwtTokenUtil.validateRefreshToken(username, refreshToken)) {
                throw BusinessException.of("刷新令牌已失效");
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtTokenUtil.generateToken(user, user.getTenantId().toString());
            String newRefreshToken = jwtTokenUtil.generateRefreshToken(user, user.getTenantId().toString());
            
            // 更新Redis中的刷新令牌
            jwtTokenUtil.storeRefreshToken(username, newRefreshToken);
            
            // 构建响应
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
            
            return ApiResponse.success("令牌刷新成功", loginResponse);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("令牌刷新失败: ", e);
            throw BusinessException.of("令牌刷新失败");
        }
    }
    
    @Override
    public ApiResponse<Void> logout(String token) {
        try {
            // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(token);
            String tenantIdStr = jwtTokenUtil.extractTenantId(token);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 从Redis中删除刷新令牌
            jwtTokenUtil.deleteRefreshToken(username);
            
            // 将令牌加入黑名单
            jwtTokenUtil.addToBlacklist(token);
            
            log.info("用户 {} 登出成功", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登出失败: ", e);
            throw BusinessException.of("登出失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> validateToken(String token) {
        try {
            // 检查令牌是否在黑名单中
            if (jwtTokenUtil.isTokenBlacklisted(token)) {
                return ApiResponse.success(false);
            }
            
            // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(token);
            String tenantIdStr = jwtTokenUtil.extractTenantId(token);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElse(null);
            
            if (user == null) {
                return ApiResponse.success(false);
            }
            
            // 验证令牌
            boolean isValid = jwtTokenUtil.validateToken(token, user);
            return ApiResponse.success(isValid);
            
        } catch (Exception e) {
            log.error("令牌验证失败: ", e);
            return ApiResponse.success(false);
        }
    }
    
    @Override
    public ApiResponse<UserInfoVO> getCurrentUser(String token) {
        try {
            // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(token);
            String tenantIdStr = jwtTokenUtil.extractTenantId(token);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 构建用户信息
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
            log.error("获取用户信息失败: ", e);
            throw BusinessException.of("获取用户信息失败");
        }
    }
    
    @Override
    public ApiResponse<Void> changePassword(String token, ChangePasswordRequest request) {
        try {
            // 验证新密码和确认密码是否一致
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw BusinessException.of("新密码和确认密码不一致");
            }
            
            // 从令牌中提取用户信息
            String username = jwtTokenUtil.extractUsername(token);
            String tenantIdStr = jwtTokenUtil.extractTenantId(token);
            Long tenantId = Long.valueOf(tenantIdStr);
            
            // 查找用户
            User user = userRepository.findByTenantIdAndUsername(tenantId, username)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证旧密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw BusinessException.of("旧密码错误");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            // 清除用户的刷新令牌，强制重新登录
            jwtTokenUtil.deleteRefreshToken(username);
            
            log.info("用户 {} 修改密码成功", username);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改密码失败: ", e);
            throw BusinessException.of("修改密码失败");
        }
    }
    
    @Override
    public ApiResponse<Void> resetPassword(String username, String tenantId) {
        try {
            // 查找用户
            User user = userRepository.findByTenantIdAndUsername(Long.valueOf(tenantId), username)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 生成默认密码（这里可以根据需求修改）
            String defaultPassword = "123456";
            user.setPassword(passwordEncoder.encode(defaultPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            // 清除用户的刷新令牌
            jwtTokenUtil.deleteRefreshToken(username);
            
            log.info("用户 {} 密码重置成功", username);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("重置密码失败: ", e);
            throw BusinessException.of("重置密码失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkUsernameExists(String username, String tenantId) {
        try {
            boolean exists = userRepository.existsByTenantIdAndUsername(Long.valueOf(tenantId), username);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            log.error("检查用户名是否存在失败: ", e);
            throw BusinessException.of("检查用户名是否存在失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkEmailExists(String email, String tenantId) {
        try {
            boolean exists = userRepository.existsByTenantIdAndEmail(Long.valueOf(tenantId), email);
            return ApiResponse.success(exists);
        } catch (Exception e) {
            log.error("检查邮箱是否存在失败: ", e);
            throw BusinessException.of("检查邮箱是否存在失败");
        }
    }
} 