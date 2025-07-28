package com.hoxkar.permission.service;

import com.hoxkar.permission.pojo.dto.*;
import com.hoxkar.permission.pojo.vo.*;
import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {
    
    // 角色管理
    /**
     * 创建角色
     */
    ApiResponse<RoleVO> createRole(CreateRoleRequest request);
    
    /**
     * 更新角色
     */
    ApiResponse<RoleVO> updateRole(Long roleId, UpdateRoleRequest request);
    
    /**
     * 删除角色
     */
    ApiResponse<Void> deleteRole(Long roleId);
    
    /**
     * 根据ID获取角色
     */
    ApiResponse<RoleVO> getRoleById(Long roleId);
    
    /**
     * 根据角色代码获取角色
     */
    ApiResponse<RoleVO> getRoleByCode(String roleCode);
    
    /**
     * 获取所有角色
     */
    ApiResponse<List<RoleVO>> getAllRoles();
    
    /**
     * 分页获取角色
     */
    ApiResponse<Page<RoleVO>> getRolesByPage(Pageable pageable);
    
    /**
     * 根据状态获取角色
     */
    ApiResponse<List<RoleVO>> getRolesByStatus(String status);
    
    /**
     * 搜索角色
     */
    ApiResponse<List<RoleVO>> searchRoles(String roleName);
    
    /**
     * 启用角色
     */
    ApiResponse<Void> enableRole(Long roleId);
    
    /**
     * 禁用角色
     */
    ApiResponse<Void> disableRole(Long roleId);
    
    /**
     * 高级搜索角色
     */
    ApiResponse<Page<RoleVO>> searchRolesAdvanced(RoleSearchRequest request);
    
    /**
     * 批量启用角色
     */
    ApiResponse<Void> batchEnableRoles(List<Long> roleIds);
    
    /**
     * 批量禁用角色
     */
    ApiResponse<Void> batchDisableRoles(List<Long> roleIds);
    
    /**
     * 批量删除角色
     */
    ApiResponse<Void> batchDeleteRoles(List<Long> roleIds);
    
    /**
     * 检查角色代码是否存在
     */
    ApiResponse<Boolean> checkRoleCodeExists(String roleCode);
    
    // 权限管理
    /**
     * 创建权限
     */
    ApiResponse<PermissionVO> createPermission(CreatePermissionRequest request);
    
    /**
     * 更新权限
     */
    ApiResponse<PermissionVO> updatePermission(Long permissionId, UpdatePermissionRequest request);
    
    /**
     * 删除权限
     */
    ApiResponse<Void> deletePermission(Long permissionId);
    
    /**
     * 根据ID获取权限
     */
    ApiResponse<PermissionVO> getPermissionById(Long permissionId);
    
    /**
     * 根据权限代码获取权限
     */
    ApiResponse<PermissionVO> getPermissionByCode(String permissionCode);
    
    /**
     * 获取所有权限
     */
    ApiResponse<List<PermissionVO>> getAllPermissions();
    
    /**
     * 分页获取权限
     */
    ApiResponse<Page<PermissionVO>> getPermissionsByPage(Pageable pageable);
    
    /**
     * 根据权限类型获取权限
     */
    ApiResponse<List<PermissionVO>> getPermissionsByType(String permissionType);
    
    /**
     * 获取权限树
     */
    ApiResponse<List<PermissionVO>> getPermissionTree();
    
    /**
     * 根据状态获取权限
     */
    ApiResponse<List<PermissionVO>> getPermissionsByStatus(String status);
    
    /**
     * 搜索权限
     */
    ApiResponse<List<PermissionVO>> searchPermissions(String permissionName);
    
    /**
     * 启用权限
     */
    ApiResponse<Void> enablePermission(Long permissionId);
    
    /**
     * 禁用权限
     */
    ApiResponse<Void> disablePermission(Long permissionId);
    
    /**
     * 高级搜索权限
     */
    ApiResponse<Page<PermissionVO>> searchPermissionsAdvanced(PermissionSearchRequest request);
    
    /**
     * 批量启用权限
     */
    ApiResponse<Void> batchEnablePermissions(List<Long> permissionIds);
    
    /**
     * 批量禁用权限
     */
    ApiResponse<Void> batchDisablePermissions(List<Long> permissionIds);
    
    /**
     * 批量删除权限
     */
    ApiResponse<Void> batchDeletePermissions(List<Long> permissionIds);
    
    /**
     * 检查权限代码是否存在
     */
    ApiResponse<Boolean> checkPermissionCodeExists(String permissionCode);
    
    // 角色权限分配
    /**
     * 为角色分配权限
     */
    ApiResponse<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds);
    
    /**
     * 获取角色的权限
     */
    ApiResponse<List<PermissionVO>> getRolePermissions(Long roleId);
    
    /**
     * 为用户分配角色
     */
    ApiResponse<Void> assignRolesToUser(AssignRoleRequest request);
    
    /**
     * 获取用户的角色
     */
    ApiResponse<List<RoleVO>> getUserRoles(Long userId);
    
    /**
     * 获取用户的权限
     */
    ApiResponse<List<PermissionVO>> getUserPermissions(Long userId);
    
    /**
     * 检查用户是否有指定权限
     */
    ApiResponse<Boolean> checkUserPermission(Long userId, String permissionCode);
    
    /**
     * 检查用户是否有指定角色
     */
    ApiResponse<Boolean> checkUserRole(Long userId, String roleCode);
    
    /**
     * 获取权限统计信息
     */
    ApiResponse<PermissionStatisticsVO> getPermissionStatistics();
} 