package com.hoxkar.auth.service;

import com.hoxkar.auth.entity.User;
import com.hoxkar.auth.repository.UserRepository;
import com.hoxkar.common.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

// Custom UserDetailsService
/**
 * Custom UserDetailsService // 自定义UserDetailsService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // Get tenant ID from tenant context // 从租户上下文中获取租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                log.error("Tenant ID is not set"); // 租户ID未设置
                throw new UsernameNotFoundException("Tenant ID is not set"); // 租户ID未设置
            }
            
            // Find user by tenant ID and username // 根据租户ID和用户名查找用户
            User user = userRepository.findByTenantIdAndUsername(Long.valueOf(tenantId), username)
                    .orElseThrow(() -> new UsernameNotFoundException("User does not exist: " + username)); // 用户不存在
            
            // Check user status // 检查用户状态
            if (!user.isEnabled()) {
                log.warn("User is disabled: {}", username); // 用户已被禁用
                throw new UsernameNotFoundException("User is disabled: " + username); // 用户已被禁用
            }
            
            log.debug("User authentication successful: {}", username); // 用户认证成功
            return user;
            
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("User authentication failed: {}", username, e); // 用户认证失败
            throw new UsernameNotFoundException("User authentication failed: " + username, e); // 用户认证失败
        }
    }
    
    /**
     * Load user by tenant ID and username // 根据租户ID和用户名加载用户
     */
    public UserDetails loadUserByTenantIdAndUsername(String tenantId, String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByTenantIdAndUsername(Long.valueOf(tenantId), username)
                    .orElseThrow(() -> new UsernameNotFoundException("User does not exist: " + username)); // 用户不存在
            
            if (!user.isEnabled()) {
                log.warn("User is disabled: {}", username); // 用户已被禁用
                throw new UsernameNotFoundException("User is disabled: " + username); // 用户已被禁用
            }
            
            log.debug("User authentication successful: {}", username); // 用户认证成功
            return user;
            
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("User authentication failed: {}", username, e); // 用户认证失败
            throw new UsernameNotFoundException("User authentication failed: " + username, e); // 用户认证失败
        }
    }
} 