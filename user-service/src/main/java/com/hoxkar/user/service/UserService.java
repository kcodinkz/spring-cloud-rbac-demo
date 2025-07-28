package com.hoxkar.user.service;

import com.hoxkar.user.pojo.dto.ChangePasswordRequest;
import com.hoxkar.user.pojo.dto.CreateUserRequest;
import com.hoxkar.user.pojo.dto.UpdateUserRequest;
import com.hoxkar.user.pojo.dto.UserSearchRequest;
import com.hoxkar.user.pojo.vo.UserStatisticsVO;
import com.hoxkar.user.pojo.vo.UserVO;
import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 创建用户
     */
    ApiResponse<UserVO> createUser(CreateUserRequest request);
    
    /**
     * 更新用户
     */
    ApiResponse<UserVO> updateUser(Long userId, UpdateUserRequest request);
    
    /**
     * 删除用户
     */
    ApiResponse<Void> deleteUser(Long userId);
    
    /**
     * 根据ID获取用户
     */
    ApiResponse<UserVO> getUserById(Long userId);
    
    /**
     * 根据用户名获取用户
     */
    ApiResponse<UserVO> getUserByUsername(String username);
    
    /**
     * 根据邮箱获取用户
     */
    ApiResponse<UserVO> getUserByEmail(String email);
    
    /**
     * 获取所有用户
     */
    ApiResponse<List<UserVO>> getAllUsers();
    
    /**
     * 分页获取用户
     */
    ApiResponse<Page<UserVO>> getUsersByPage(Pageable pageable);
    
    /**
     * 根据状态获取用户
     */
    ApiResponse<List<UserVO>> getUsersByStatus(String status);
    
    /**
     * 根据真实姓名搜索用户
     */
    ApiResponse<List<UserVO>> searchUsersByRealName(String realName);
    
    /**
     * 修改密码
     */
    ApiResponse<Void> changePassword(Long userId, ChangePasswordRequest request);
    
    /**
     * 启用用户
     */
    ApiResponse<Void> enableUser(Long userId);
    
    /**
     * 禁用用户
     */
    ApiResponse<Void> disableUser(Long userId);
    
    /**
     * 重置密码
     */
    ApiResponse<Void> resetPassword(Long userId, String newPassword);
    
    /**
     * 高级搜索用户
     */
    ApiResponse<Page<UserVO>> searchUsers(UserSearchRequest request);
    
    /**
     * 根据是否为超级管理员获取用户
     */
    ApiResponse<List<UserVO>> getUsersBySuperAdmin(Boolean isSuperAdmin);
    
    /**
     * 批量启用用户
     */
    ApiResponse<Void> batchEnableUsers(List<Long> userIds);
    
    /**
     * 批量禁用用户
     */
    ApiResponse<Void> batchDisableUsers(List<Long> userIds);
    
    /**
     * 批量删除用户
     */
    ApiResponse<Void> batchDeleteUsers(List<Long> userIds);
    
    /**
     * 获取用户统计信息
     */
    ApiResponse<UserStatisticsVO> getUserStatistics();
    
    /**
     * 检查用户名是否存在
     */
    ApiResponse<Boolean> checkUsernameExists(String username);
    
    /**
     * 检查邮箱是否存在
     */
    ApiResponse<Boolean> checkEmailExists(String email);
} 