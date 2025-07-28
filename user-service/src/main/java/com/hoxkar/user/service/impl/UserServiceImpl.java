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
            // 设置租户上下文
            TenantContext.setTenantId(request.getTenantId());
            
            // 检查用户名是否已存在
            if (userRepository.existsByTenantIdAndUsername(Long.valueOf(request.getTenantId()), request.getUsername())) {
                throw BusinessException.of("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (request.getEmail() != null && userRepository.existsByTenantIdAndEmail(Long.valueOf(request.getTenantId()), request.getEmail())) {
                throw BusinessException.of("邮箱已存在");
            }
            
            // 创建用户实体
            User user = new User();
            user.setTenantId(Long.valueOf(request.getTenantId()));
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setRealName(request.getRealName());
            user.setIsSuperAdmin(request.getIsSuperAdmin());
            user.setStatus("ACTIVE");
            
            // 保存用户
            User savedUser = userRepository.save(user);
            
            log.info("创建用户成功: {}", savedUser.getUsername());
            return ApiResponse.success("用户创建成功", convertToVO(savedUser));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建用户失败: ", e);
            throw BusinessException.of("创建用户失败");
        } finally {
            TenantContext.clear();
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<UserVO> updateUser(Long userId, UpdateUserRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此用户");
            }
            
            // 检查邮箱是否已被其他用户使用
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByTenantIdAndEmail(Long.valueOf(tenantId), request.getEmail())) {
                    throw BusinessException.of("邮箱已被其他用户使用");
                }
                user.setEmail(request.getEmail());
            }
            
            // 更新用户信息
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
            
            // 保存用户
            User updatedUser = userRepository.save(user);
            
            log.info("更新用户成功: {}", updatedUser.getUsername());
            return ApiResponse.success("用户更新成功", convertToVO(updatedUser));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新用户失败: ", e);
            throw BusinessException.of("更新用户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteUser(Long userId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此用户");
            }
            
            // 删除用户
            userRepository.delete(user);
            
            log.info("用户 {} 删除成功", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除用户失败: ", e);
            throw BusinessException.of("删除用户失败");
        }
    }
    
    @Override
    public ApiResponse<UserVO> getUserById(Long userId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限查看此用户");
            }
            
            return ApiResponse.success(convertToVO(user));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户失败: ", e);
            throw BusinessException.of("获取用户失败");
        }
    }
    
    @Override
    public ApiResponse<UserVO> getUserByUsername(String username) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findByTenantIdAndUsername(Long.valueOf(tenantId), username)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            return ApiResponse.success(convertToVO(user));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户失败: ", e);
            throw BusinessException.of("获取用户失败");
        }
    }
    
    @Override
    public ApiResponse<UserVO> getUserByEmail(String email) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findByTenantIdAndEmail(Long.valueOf(tenantId), email)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            return ApiResponse.success(convertToVO(user));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户失败: ", e);
            throw BusinessException.of("获取用户失败");
        }
    }
    
    @Override
    public ApiResponse<List<UserVO>> getAllUsers() {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找所有用户
            List<User> users = userRepository.findAllByTenantId(Long.valueOf(tenantId));
            List<UserVO> userVOs = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(userVOs);
            
        } catch (Exception e) {
            log.error("获取用户列表失败: ", e);
            throw BusinessException.of("获取用户列表失败");
        }
    }
    
    @Override
    public ApiResponse<Page<UserVO>> getUsersByPage(Pageable pageable) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 分页查找用户
            Page<User> userPage = userRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            Page<UserVO> userVOPage = userPage.map(this::convertToVO);
            
            return ApiResponse.success(userVOPage);
            
        } catch (Exception e) {
            log.error("分页获取用户失败: ", e);
            throw BusinessException.of("分页获取用户失败");
        }
    }
    
    @Override
    public ApiResponse<List<UserVO>> getUsersByStatus(String status) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据状态查找用户
            List<User> users = userRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), status);
            List<UserVO> userVOs = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(userVOs);
            
        } catch (Exception e) {
            log.error("根据状态获取用户失败: ", e);
            throw BusinessException.of("根据状态获取用户失败");
        }
    }
    
    @Override
    public ApiResponse<List<UserVO>> searchUsersByRealName(String realName) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据真实姓名搜索用户
            List<User> users = userRepository.findByTenantIdAndRealNameContaining(Long.valueOf(tenantId), realName);
            List<UserVO> userVOs = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(userVOs);
            
        } catch (Exception e) {
            log.error("搜索用户失败: ", e);
            throw BusinessException.of("搜索用户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> changePassword(Long userId, ChangePasswordRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此用户");
            }
            
            // 验证旧密码
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw BusinessException.of("旧密码不正确");
            }
            
            // 验证新密码确认
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw BusinessException.of("新密码与确认密码不一致");
            }
            
            // 更新密码
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
            
            log.info("用户 {} 密码修改成功", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改密码失败: ", e);
            throw BusinessException.of("修改密码失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> enableUser(Long userId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此用户");
            }
            
            // 启用用户
            user.setStatus("ACTIVE");
            userRepository.save(user);
            
            log.info("用户 {} 启用成功", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("启用用户失败: ", e);
            throw BusinessException.of("启用用户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> disableUser(Long userId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此用户");
            }
            
            // 禁用用户
            user.setStatus("INACTIVE");
            userRepository.save(user);
            
            log.info("用户 {} 禁用成功", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("禁用用户失败: ", e);
            throw BusinessException.of("禁用用户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> resetPassword(Long userId, String newPassword) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找用户
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of("用户不存在"));
            
            // 验证租户权限
            if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此用户");
            }
            
            // 重置密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            log.info("用户 {} 密码重置成功", user.getUsername());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("重置密码失败: ", e);
            throw BusinessException.of("重置密码失败");
        }
    }
    
    @Override
    public ApiResponse<Page<UserVO>> searchUsers(UserSearchRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 构建分页参数
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            
            // 根据搜索条件查找用户
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
                // 默认查询所有用户
                userPage = userRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            }
            
            if (userPage != null) {
                Page<UserVO> userVOPage = userPage.map(this::convertToVO);
                return ApiResponse.success(userVOPage);
            } else {
                // 处理手动分页的情况
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("高级搜索用户失败: ", e);
            throw BusinessException.of("高级搜索用户失败");
        }
    }
    
    @Override
    public ApiResponse<List<UserVO>> getUsersBySuperAdmin(Boolean isSuperAdmin) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据是否为超级管理员查找用户
            List<User> users = userRepository.findByTenantIdAndIsSuperAdmin(Long.valueOf(tenantId), isSuperAdmin);
            List<UserVO> userVOs = users.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(userVOs);
            
        } catch (Exception e) {
            log.error("根据超级管理员状态获取用户失败: ", e);
            throw BusinessException.of("根据超级管理员状态获取用户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchEnableUsers(List<Long> userIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量启用用户
            for (Long userId : userIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> BusinessException.of("用户不存在: " + userId));
                
                // 验证租户权限
                if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作用户: " + userId);
                }
                
                user.setStatus("ACTIVE");
                userRepository.save(user);
            }
            
            log.info("批量启用用户成功: {}", userIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量启用用户失败: ", e);
            throw BusinessException.of("批量启用用户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDisableUsers(List<Long> userIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量禁用用户
            for (Long userId : userIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> BusinessException.of("用户不存在: " + userId));
                
                // 验证租户权限
                if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作用户: " + userId);
                }
                
                user.setStatus("INACTIVE");
                userRepository.save(user);
            }
            
            log.info("批量禁用用户成功: {}", userIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量禁用用户失败: ", e);
            throw BusinessException.of("批量禁用用户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeleteUsers(List<Long> userIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量删除用户
            for (Long userId : userIds) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> BusinessException.of("用户不存在: " + userId));
                
                // 验证租户权限
                if (!user.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作用户: " + userId);
                }
                
                userRepository.delete(user);
            }
            
            log.info("批量删除用户成功: {}", userIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除用户失败: ", e);
            throw BusinessException.of("批量删除用户失败");
        }
    }
    
    @Override
    public ApiResponse<UserStatisticsVO> getUserStatistics() {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 计算本月开始时间
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            // 获取统计信息
            Long totalUsers = userRepository.countByTenantId(Long.valueOf(tenantId));
            Long activeUsers = userRepository.countByTenantIdAndStatusActive(Long.valueOf(tenantId));
            Long inactiveUsers = userRepository.countByTenantIdAndStatusInactive(Long.valueOf(tenantId));
            Long superAdminUsers = userRepository.countByTenantIdAndIsSuperAdminTrue(Long.valueOf(tenantId));
            Long normalUsers = userRepository.countByTenantIdAndIsSuperAdminFalse(Long.valueOf(tenantId));
            Long newUsersThisMonth = userRepository.countByTenantIdAndCreatedAtAfter(Long.valueOf(tenantId), startOfMonth);
            Long activeUsersThisMonth = userRepository.countByTenantIdAndLastLoginAtAfter(Long.valueOf(tenantId), startOfMonth);
            
            // 构建统计信息
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
            log.error("获取用户统计信息失败: ", e);
            throw BusinessException.of("获取用户统计信息失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkUsernameExists(String username) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = userRepository.existsByTenantIdAndUsername(Long.valueOf(tenantId), username);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查用户名是否存在失败: ", e);
            throw BusinessException.of("检查用户名是否存在失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkEmailExists(String email) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = userRepository.existsByTenantIdAndEmail(Long.valueOf(tenantId), email);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查邮箱是否存在失败: ", e);
            throw BusinessException.of("检查邮箱是否存在失败");
        }
    }
    
    /**
     * 将User实体转换为UserVO
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