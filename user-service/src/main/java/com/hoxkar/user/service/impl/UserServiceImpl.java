package com.hoxkar.user.service.impl;

import com.hoxkar.user.entity.User;
import com.hoxkar.user.pojo.dto.ChangePasswordRequest;
import com.hoxkar.user.pojo.dto.CreateUserRequest;
import com.hoxkar.user.pojo.dto.UpdateUserRequest;
import com.hoxkar.user.pojo.dto.UserSearchRequest;
import com.hoxkar.user.pojo.vo.UserStatisticsVO;
import com.hoxkar.user.pojo.vo.UserVO;
import com.hoxkar.user.repository.UserRepository;
import com.hoxkar.user.service.UserService;
import com.hoxkar.common.exception.BusinessException;
import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.common.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// User Service Implementation
/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public ApiResponse<UserVO> createUser(CreateUserRequest request) {
        try {
            // Set tenant context // 设置租户上下文
            TenantContext.setTenantId(request.getTenantId());
            
            // Check if username already exists // 检查用户名是否已存在
            if (userRepository.existsByTenantIdAndUsername(Long.valueOf(request.getTenantId()), request.getUsername())) {
                throw BusinessException.of("Username already exists"); // 用户名已存在
            }
            
            // Check if email already exists // 检查邮箱是否已存在
            if (request.getEmail() != null && userRepository.existsByTenantIdAndEmail(Long.valueOf(request.getTenantId()), request.getEmail())) {
                throw BusinessException.of("Email already exists"); // 邮箱已存在
            }
            
            // Create user entity // 创建用户实体
            User user = new User();
            user.setTenantId(Long.valueOf(request.getTenantId()));
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRealName(request.getRealName());
            user.setIsSuperAdmin(request.getIsSuperAdmin());
            user.setStatus("ACTIVE");
            
            // Save user // 保存用户
            User savedUser = userRepository.save(user);
            
            log.info("User created successfully: {}", savedUser.getUsername()); // 创建用户成功
            return ApiResponse.success("User created successfully", convertToVO(savedUser)); // 用户创建成功
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("User creation failed: ", e); // 创建用户失败
            throw BusinessException.of("User creation failed"); // 创建用户失败
        } finally {
            TenantContext.clear();
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<UserVO> updateUser(Long userId, UpdateUserRequest request) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            // Validate tenant permission // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this user");
            }
            
            // Check if email is already used by another user // 检查邮箱是否已被其他用户使用
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByTenantIdAndEmail(Long.valueOf(tenantId), request.getEmail())) {
                    throw BusinessException.of("Email is already in use by another user");
                }
                user.setEmail(request.getEmail());
            }
            
            // Update user information // 更新用户信息
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
            if (request.getRealName() != null) {
                user.setRealName(request.getRealName());
            }
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }
            if (request.getIsSuperAdmin() != null) {
                user.setIsSuperAdmin(request.getIsSuperAdmin());
            }
            
            // Save user // 保存用户
            User updatedUser = userRepository.save(user);
            
            log.info("User updated successfully: {}", updatedUser.getUsername());
            return ApiResponse.success("User updated successfully", convertToVO(updatedUser));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("User update failed: ", e);
            throw BusinessException.of("User update failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Long userId) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            // Validate tenant permission // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this user");
            }
            
            // Delete user // 删除用户
            userRepository.delete(user);
            
            log.info("User {} deleted successfully", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("User deletion failed: ", e);
            throw BusinessException.of("User deletion failed");
        }
    }
    
    @Override
    public ApiResponse<UserVO> getUserById(Long userId) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            // Validate tenant permission // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to view this user");
            }
            
            return ApiResponse.success(convertToVO(user));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get user: ", e);
            throw BusinessException.of("Failed to get user");
        }
    }
    
    @Override
    public ApiResponse<UserVO> getUserByUsername(String username) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findByTenantIdAndUsername(Long.valueOf(tenantId), username)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            return ApiResponse.success(convertToVO(user));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get user: ", e);
            throw BusinessException.of("Failed to get user");
        }
    }
    
    @Override
    public ApiResponse<UserVO> getUserByEmail(String email) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findByTenantIdAndEmail(Long.valueOf(tenantId), email)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            return ApiResponse.success(convertToVO(user));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get user: ", e);
            throw BusinessException.of("Failed to get user");
        }
    }
    
    @Override
    public ApiResponse<List<UserVO>> getAllUsers() {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find all users // 查找所有用户
            List<User> users = userRepository.findAllByTenantId(Long.valueOf(tenantId));
            List<UserVO> userVOs = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(userVOs);
            
        } catch (Exception e) {
            log.error("Failed to get user list: ", e);
            throw BusinessException.of("Failed to get user list");
        }
    }
    
    @Override
    public ApiResponse<Page<UserVO>> getUsersByPage(Pageable pageable) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Paginate users // 分页查找用户
            Page<User> userPage = userRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            Page<UserVO> userVOPage = userPage.map(this::convertToVO);
            
            return ApiResponse.success(userVOPage);
            
        } catch (Exception e) {
            log.error("Failed to paginate users: ", e);
            throw BusinessException.of("Failed to paginate users");
        }
    }
    
    @Override
    public ApiResponse<List<UserVO>> getUsersByStatus(String status) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find users by status // 根据状态查找用户
            List<User> users = userRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), status);
            List<UserVO> userVOs = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(userVOs);
            
        } catch (Exception e) {
            log.error("Failed to get users by status: ", e);
            throw BusinessException.of("Failed to get users by status");
        }
    }
    
    @Override
    public ApiResponse<List<UserVO>> searchUsersByRealName(String realName) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Search users by real name // 根据真实姓名搜索用户
            List<User> users = userRepository.findByTenantIdAndRealNameContaining(Long.valueOf(tenantId), realName);
            List<UserVO> userVOs = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(userVOs);
            
        } catch (Exception e) {
            log.error("Search users failed: ", e);
            throw BusinessException.of("Search users failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> changePassword(Long userId, ChangePasswordRequest request) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            // Validate tenant permission // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this user");
            }
            
            // Validate old password // 验证旧密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw BusinessException.of("Old password is incorrect");
            }
            
            // Validate new password confirmation // 验证新密码确认
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw BusinessException.of("New password and confirmation password do not match");
            }
            
            // Update password // 更新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            log.info("User {} password changed successfully", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Password change failed: ", e);
            throw BusinessException.of("Password change failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> enableUser(Long userId) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            // Validate tenant permission // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this user");
            }
            
            // Enable user // 启用用户
            user.setStatus("ACTIVE");
            userRepository.save(user);
            
            log.info("User {} enabled successfully", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Enable user failed: ", e);
            throw BusinessException.of("Enable user failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> disableUser(Long userId) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            // Validate tenant permission // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this user");
            }
            
            // Disable user // 禁用用户
            user.setStatus("INACTIVE");
            userRepository.save(user);
            
            log.info("User {} disabled successfully", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Disable user failed: ", e);
            throw BusinessException.of("Disable user failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> resetPassword(Long userId, String newPassword) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find user // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("User not found"));
            
            // Validate tenant permission // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this user");
            }
            
            // Reset password // 重置密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            log.info("User {} password reset successfully", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Password reset failed: ", e);
            throw BusinessException.of("Password reset failed");
        }
    }
    
    @Override
    public ApiResponse<Page<UserVO>> searchUsers(UserSearchRequest request) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Build pagination parameters // 构建分页参数
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            
            // Find users based on search criteria // 根据搜索条件查找用户
            Page<User> userPage = null;
            
            if (StringUtils.hasText(request.getUsername())) {
                userPage = userRepository.findByTenantIdAndUsernameContaining(Long.valueOf(tenantId), request.getUsername(), pageable);
            } else if (StringUtils.hasText(request.getRealName())) {
                userPage = userRepository.findByTenantIdAndRealNameContaining(Long.valueOf(tenantId), request.getRealName(), pageable);
            } else if (StringUtils.hasText(request.getEmail())) {
                userPage = userRepository.findByTenantIdAndEmailContaining(Long.valueOf(tenantId), request.getEmail(), pageable);
            } else if (StringUtils.hasText(request.getPhone())) {
                userPage = userRepository.findByTenantIdAndPhoneContaining(Long.valueOf(tenantId), request.getPhone(), pageable);
            } else if (StringUtils.hasText(request.getStatus())) {
                userPage = userRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), request.getStatus(), pageable);
            } else if (request.getIsSuperAdmin() != null) {
                userPage = userRepository.findByTenantIdAndIsSuperAdmin(Long.valueOf(tenantId), request.getIsSuperAdmin(), pageable);
            } else {
                // Default query all users // 默认查询所有用户
                userPage = userRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            }
            
            if (userPage != null) {
                Page<UserVO> userVOPage = userPage.map(this::convertToVO);
                return ApiResponse.success(userVOPage);
            } else {
                // Handle manual pagination // 处理手动分页的情况
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("Advanced user search failed: ", e);
            throw BusinessException.of("Advanced user search failed");
        }
    }
    
    @Override
    public ApiResponse<List<UserVO>> getUsersBySuperAdmin(Boolean isSuperAdmin) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find users by super admin status // 根据是否为超级管理员查找用户
            List<User> users = userRepository.findByTenantIdAndIsSuperAdmin(Long.valueOf(tenantId), isSuperAdmin);
            List<UserVO> userVOs = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(userVOs);
            
        } catch (Exception e) {
            log.error("Failed to get users by super admin status: ", e);
            throw BusinessException.of("Failed to get users by super admin status");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchEnableUsers(List<Long> userIds) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch enable users // 批量启用用户
            for (Long userId : userIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> BusinessException.of("User not found: " + userId));
                
                // Validate tenant permission // 验证租户权限
                if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate user: " + userId);
                }
                
                user.setStatus("ACTIVE");
                userRepository.save(user);
            }
            
            log.info("Batch enable users successful: {}", userIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch enable users failed: ", e);
            throw BusinessException.of("Batch enable users failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDisableUsers(List<Long> userIds) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch disable users // 批量禁用用户
            for (Long userId : userIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> BusinessException.of("User not found: " + userId));
                
                // Validate tenant permission // 验证租户权限
                if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate user: " + userId);
                }
                
                user.setStatus("INACTIVE");
                userRepository.save(user);
            }
            
            log.info("Batch disable users successful: {}", userIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch disable users failed: ", e);
            throw BusinessException.of("Batch disable users failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeleteUsers(List<Long> userIds) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch delete users // 批量删除用户
            for (Long userId : userIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> BusinessException.of("User not found: " + userId));
                
                // Validate tenant permission // 验证租户权限
                if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate user: " + userId);
                }
                
                userRepository.delete(user);
            }
            
            log.info("Batch delete users successful: {}", userIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch delete users failed: ", e);
            throw BusinessException.of("Batch delete users failed");
        }
    }
    
    @Override
    public ApiResponse<UserStatisticsVO> getUserStatistics() {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Calculate start of month // 计算本月开始时间
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            // Get statistics // 获取统计信息
            Long totalUsers = userRepository.countByTenantId(Long.valueOf(tenantId));
            Long activeUsers = userRepository.countByTenantIdAndStatusActive(Long.valueOf(tenantId));
            Long inactiveUsers = userRepository.countByTenantIdAndStatusInactive(Long.valueOf(tenantId));
            Long superAdminUsers = userRepository.countByTenantIdAndIsSuperAdminTrue(Long.valueOf(tenantId));
            Long normalUsers = userRepository.countByTenantIdAndIsSuperAdminFalse(Long.valueOf(tenantId));
            Long newUsersThisMonth = userRepository.countByTenantIdAndCreatedAtAfter(Long.valueOf(tenantId), startOfMonth);
            Long activeUsersThisMonth = userRepository.countByTenantIdAndLastLoginAtAfter(Long.valueOf(tenantId), startOfMonth);
            
            // Build statistics // 构建统计信息
            UserStatisticsVO statistics = new UserStatisticsVO();
            statistics.setTotalUsers(totalUsers);
            statistics.setActiveUsers(activeUsers);
            statistics.setInactiveUsers(inactiveUsers);
            statistics.setSuperAdminUsers(superAdminUsers);
            statistics.setNormalUsers(normalUsers);
            statistics.setNewUsersThisMonth(newUsersThisMonth);
            statistics.setActiveUsersThisMonth(activeUsersThisMonth);
            
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("Failed to get user statistics: ", e);
            throw BusinessException.of("Failed to get user statistics");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkUsernameExists(String username) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = userRepository.existsByTenantIdAndUsername(Long.valueOf(tenantId), username);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("Failed to check if username exists: ", e);
            throw BusinessException.of("Failed to check if username exists");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkEmailExists(String email) {
        try {
            // Get current tenant ID // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = userRepository.existsByTenantIdAndEmail(Long.valueOf(tenantId), email);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("Failed to check if email exists: ", e);
            throw BusinessException.of("Failed to check if email exists");
        }
    }
    
    /**
     * Convert User entity to UserVO // 将User实体转换为UserVO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setTenantId(user.getTenantId().toString());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setStatus(user.getStatus());
        vo.setIsSuperAdmin(user.getIsSuperAdmin());
        vo.setLastLoginTime(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
} 