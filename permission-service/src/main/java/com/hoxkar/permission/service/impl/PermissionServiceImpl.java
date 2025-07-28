package com.hoxkar.permission.service.impl;

import com.hoxkar.permission.entity.Permission;
import com.hoxkar.permission.entity.Role;
import com.hoxkar.permission.entity.UserRole;
import com.hoxkar.permission.pojo.dto.*;
import com.hoxkar.permission.pojo.vo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import com.hoxkar.permission.repository.PermissionRepository;
import com.hoxkar.permission.repository.RoleRepository;
import com.hoxkar.permission.repository.UserRoleRepository;
import com.hoxkar.permission.service.PermissionService;
import com.hoxkar.common.exception.BusinessException;
import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.common.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    
    // 角色管理
    @Override
    @Transactional
    public ApiResponse<RoleVO> createRole(CreateRoleRequest request) {
        try {
            // 设置租户上下文
            TenantContext.setTenantId(request.getTenantId());
            
            // 检查角色代码是否已存在
            if (roleRepository.existsByTenantIdAndRoleCode(Long.valueOf(request.getTenantId()), request.getRoleCode())) {
                throw BusinessException.of("角色代码已存在");
            }
            
            // 创建角色实体
            Role role = new Role();
            role.setTenantId(Long.valueOf(request.getTenantId()));
            role.setRoleName(request.getRoleName());
            role.setRoleCode(request.getRoleCode());
            role.setDescription(request.getDescription());
            role.setIsSystem(request.getIsSystem());
            role.setStatus("ACTIVE");
            
            // 保存角色
            Role savedRole = roleRepository.save(role);
            
            // 分配权限
            if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
                assignPermissionsToRole(savedRole.getId(), request.getPermissionIds());
            }
            
            log.info("创建角色成功: {}", savedRole.getRoleName());
            return ApiResponse.success("角色创建成功", convertToRoleVO(savedRole));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建角色失败: ", e);
            throw BusinessException.of("创建角色失败");
        } finally {
            TenantContext.clear();
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<RoleVO> updateRole(Long roleId, UpdateRoleRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("角色不存在"));
            
            // 验证租户权限
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此角色");
            }
            
            // 检查角色代码是否已被其他角色使用
            if (request.getRoleCode() != null && !request.getRoleCode().equals(role.getRoleCode())) {
                if (roleRepository.existsByTenantIdAndRoleCode(Long.valueOf(tenantId), request.getRoleCode())) {
                    throw BusinessException.of("角色代码已被其他角色使用");
                }
                role.setRoleCode(request.getRoleCode());
            }
            
            // 更新角色信息
            if (request.getRoleName() != null) {
                role.setRoleName(request.getRoleName());
            }
            if (request.getDescription() != null) {
                role.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                role.setStatus(request.getStatus());
            }
            
            // 保存角色
            Role updatedRole = roleRepository.save(role);
            
            // 更新权限分配
            if (request.getPermissionIds() != null) {
                assignPermissionsToRole(roleId, request.getPermissionIds());
            }
            
            log.info("更新角色成功: {}", updatedRole.getRoleName());
            return ApiResponse.success("角色更新成功", convertToRoleVO(updatedRole));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新角色失败: ", e);
            throw BusinessException.of("更新角色失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteRole(Long roleId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("角色不存在"));
            
            // 验证租户权限
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此角色");
            }
            
            // 检查是否为系统角色
            if (role.getIsSystem()) {
                throw BusinessException.of("系统角色不能删除");
            }
            
            // 删除角色
            roleRepository.delete(role);
            
            log.info("角色 {} 删除成功", role.getRoleName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除角色失败: ", e);
            throw BusinessException.of("删除角色失败");
        }
    }
    
    @Override
    public ApiResponse<RoleVO> getRoleById(Long roleId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("角色不存在"));
            
            // 验证租户权限
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限查看此角色");
            }
            
            return ApiResponse.success(convertToRoleVO(role));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取角色失败: ", e);
            throw BusinessException.of("获取角色失败");
        }
    }
    
    @Override
    public ApiResponse<RoleVO> getRoleByCode(String roleCode) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找角色
            Role role = roleRepository.findByTenantIdAndRoleCode(Long.valueOf(tenantId), roleCode)
                    .orElseThrow(() -> BusinessException.of("角色不存在"));
            
            return ApiResponse.success(convertToRoleVO(role));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取角色失败: ", e);
            throw BusinessException.of("获取角色失败");
        }
    }
    
    @Override
    public ApiResponse<List<RoleVO>> getAllRoles() {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找所有角色
            List<Role> roles = roleRepository.findAllByTenantId(Long.valueOf(tenantId));
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(roleVOs);
            
        } catch (Exception e) {
            log.error("获取角色列表失败: ", e);
            throw BusinessException.of("获取角色列表失败");
        }
    }
    
    @Override
    public ApiResponse<Page<RoleVO>> getRolesByPage(Pageable pageable) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 分页查找角色
            Page<Role> rolePage = roleRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            Page<RoleVO> roleVOPage = rolePage.map(this::convertToRoleVO);
            
            return ApiResponse.success(roleVOPage);
            
        } catch (Exception e) {
            log.error("分页获取角色失败: ", e);
            throw BusinessException.of("分页获取角色失败");
        }
    }
    
    @Override
    public ApiResponse<List<RoleVO>> getRolesByStatus(String status) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据状态查找角色
            List<Role> roles = roleRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), status);
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(roleVOs);
            
        } catch (Exception e) {
            log.error("根据状态获取角色失败: ", e);
            throw BusinessException.of("根据状态获取角色失败");
        }
    }
    
    @Override
    public ApiResponse<List<RoleVO>> searchRoles(String roleName) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据角色名称搜索角色
            List<Role> roles = roleRepository.findByTenantIdAndRoleNameContaining(Long.valueOf(tenantId), roleName);
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(roleVOs);
            
        } catch (Exception e) {
            log.error("搜索角色失败: ", e);
            throw BusinessException.of("搜索角色失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> enableRole(Long roleId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("角色不存在"));
            
            // 验证租户权限
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此角色");
            }
            
            // 启用角色
            role.setStatus("ACTIVE");
            roleRepository.save(role);
            
            log.info("角色 {} 启用成功", role.getRoleName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("启用角色失败: ", e);
            throw BusinessException.of("启用角色失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> disableRole(Long roleId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("角色不存在"));
            
            // 验证租户权限
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此角色");
            }
            
            // 禁用角色
            role.setStatus("INACTIVE");
            roleRepository.save(role);
            
            log.info("角色 {} 禁用成功", role.getRoleName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("禁用角色失败: ", e);
            throw BusinessException.of("禁用角色失败");
        }
    }
    
    // 权限管理
    @Override
    @Transactional
    public ApiResponse<PermissionVO> createPermission(CreatePermissionRequest request) {
        try {
            // 设置租户上下文
            TenantContext.setTenantId(request.getTenantId());
            
            // 检查权限代码是否已存在
            if (permissionRepository.existsByTenantIdAndPermissionCode(Long.valueOf(request.getTenantId()), request.getPermissionCode())) {
                throw BusinessException.of("权限代码已存在");
            }
            
            // 创建权限实体
            Permission permission = new Permission();
            permission.setTenantId(Long.valueOf(request.getTenantId()));
            permission.setPermissionName(request.getPermissionName());
            permission.setPermissionCode(request.getPermissionCode());
            permission.setPermissionType(request.getPermissionType());
            permission.setParentId(request.getParentId());
            permission.setPath(request.getPath());
            permission.setComponent(request.getComponent());
            permission.setIcon(request.getIcon());
            permission.setSortOrder(request.getSortOrder());
            permission.setDescription(request.getDescription());
            permission.setIsSystem(request.getIsSystem());
            permission.setStatus("ACTIVE");
            
            // 保存权限
            Permission savedPermission = permissionRepository.save(permission);
            
            log.info("创建权限成功: {}", savedPermission.getPermissionName());
            return ApiResponse.success("权限创建成功", convertToPermissionVO(savedPermission));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建权限失败: ", e);
            throw BusinessException.of("创建权限失败");
        } finally {
            TenantContext.clear();
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<PermissionVO> updatePermission(Long permissionId, UpdatePermissionRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找权限
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("权限不存在"));
            
            // 验证租户权限
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此权限");
            }
            
            // 检查权限代码是否已被其他权限使用
            if (request.getPermissionCode() != null && !request.getPermissionCode().equals(permission.getPermissionCode())) {
                if (permissionRepository.existsByTenantIdAndPermissionCode(Long.valueOf(tenantId), request.getPermissionCode())) {
                    throw BusinessException.of("权限代码已被其他权限使用");
                }
                permission.setPermissionCode(request.getPermissionCode());
            }
            
            // 更新权限信息
            if (request.getPermissionName() != null) {
                permission.setPermissionName(request.getPermissionName());
            }
            if (request.getPermissionType() != null) {
                permission.setPermissionType(request.getPermissionType());
            }
            if (request.getParentId() != null) {
                permission.setParentId(request.getParentId());
            }
            if (request.getPath() != null) {
                permission.setPath(request.getPath());
            }
            if (request.getComponent() != null) {
                permission.setComponent(request.getComponent());
            }
            if (request.getIcon() != null) {
                permission.setIcon(request.getIcon());
            }
            if (request.getSortOrder() != null) {
                permission.setSortOrder(request.getSortOrder());
            }
            if (request.getDescription() != null) {
                permission.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                permission.setStatus(request.getStatus());
            }
            
            // 保存权限
            Permission updatedPermission = permissionRepository.save(permission);
            
            log.info("更新权限成功: {}", updatedPermission.getPermissionName());
            return ApiResponse.success("权限更新成功", convertToPermissionVO(updatedPermission));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新权限失败: ", e);
            throw BusinessException.of("更新权限失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deletePermission(Long permissionId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找权限
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("权限不存在"));
            
            // 验证租户权限
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此权限");
            }
            
            // 检查是否为系统权限
            if (permission.getIsSystem()) {
                throw BusinessException.of("系统权限不能删除");
            }
            
            // 检查是否有子权限
            List<Permission> children = permissionRepository.findByTenantIdAndParentId(Long.valueOf(tenantId), permissionId);
            if (!children.isEmpty()) {
                throw BusinessException.of("存在子权限，不能删除");
            }
            
            // 删除权限
            permissionRepository.delete(permission);
            
            log.info("权限 {} 删除成功", permission.getPermissionName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除权限失败: ", e);
            throw BusinessException.of("删除权限失败");
        }
    }
    
    @Override
    public ApiResponse<PermissionVO> getPermissionById(Long permissionId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找权限
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("权限不存在"));
            
            // 验证租户权限
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限查看此权限");
            }
            
            return ApiResponse.success(convertToPermissionVO(permission));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取权限失败: ", e);
            throw BusinessException.of("获取权限失败");
        }
    }
    
    @Override
    public ApiResponse<PermissionVO> getPermissionByCode(String permissionCode) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找权限
            Permission permission = permissionRepository.findByTenantIdAndPermissionCode(Long.valueOf(tenantId), permissionCode)
                    .orElseThrow(() -> BusinessException.of("权限不存在"));
            
            return ApiResponse.success(convertToPermissionVO(permission));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取权限失败: ", e);
            throw BusinessException.of("获取权限失败");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getAllPermissions() {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找所有权限
            List<Permission> permissions = permissionRepository.findAllByTenantId(Long.valueOf(tenantId));
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("获取权限列表失败: ", e);
            throw BusinessException.of("获取权限列表失败");
        }
    }
    
    @Override
    public ApiResponse<Page<PermissionVO>> getPermissionsByPage(Pageable pageable) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 分页查找权限
            Page<Permission> permissionPage = permissionRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            Page<PermissionVO> permissionVOPage = permissionPage.map(this::convertToPermissionVO);
            
            return ApiResponse.success(permissionVOPage);
            
        } catch (Exception e) {
            log.error("分页获取权限失败: ", e);
            throw BusinessException.of("分页获取权限失败");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getPermissionsByType(String permissionType) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据权限类型查找权限
            List<Permission> permissions = permissionRepository.findByTenantIdAndPermissionType(Long.valueOf(tenantId), permissionType);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("根据权限类型获取权限失败: ", e);
            throw BusinessException.of("根据权限类型获取权限失败");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getPermissionTree() {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找所有权限
            List<Permission> permissions = permissionRepository.findAllByTenantId(Long.valueOf(tenantId));
            
            // 构建权限树
            List<PermissionVO> permissionTree = buildPermissionTree(permissions);
            
            return ApiResponse.success(permissionTree);
            
        } catch (Exception e) {
            log.error("获取权限树失败: ", e);
            throw BusinessException.of("获取权限树失败");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getPermissionsByStatus(String status) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据状态查找权限
            List<Permission> permissions = permissionRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), status);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("根据状态获取权限失败: ", e);
            throw BusinessException.of("根据状态获取权限失败");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> searchPermissions(String permissionName) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据权限名称搜索权限
            List<Permission> permissions = permissionRepository.findByTenantIdAndPermissionNameContaining(Long.valueOf(tenantId), permissionName);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("搜索权限失败: ", e);
            throw BusinessException.of("搜索权限失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> enablePermission(Long permissionId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找权限
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("权限不存在"));
            
            // 验证租户权限
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此权限");
            }
            
            // 启用权限
            permission.setStatus("ACTIVE");
            permissionRepository.save(permission);
            
            log.info("权限 {} 启用成功", permission.getPermissionName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("启用权限失败: ", e);
            throw BusinessException.of("启用权限失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> disablePermission(Long permissionId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找权限
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("权限不存在"));
            
            // 验证租户权限
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此权限");
            }
            
            // 禁用权限
            permission.setStatus("INACTIVE");
            permissionRepository.save(permission);
            
            log.info("权限 {} 禁用成功", permission.getPermissionName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("禁用权限失败: ", e);
            throw BusinessException.of("禁用权限失败");
        }
    }
    
    // 角色权限分配
    @Override
    @Transactional
    public ApiResponse<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("角色不存在"));
            
            // 验证租户权限
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限操作此角色");
            }
            
            // 查找权限
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            
            // 验证权限是否属于同一租户
            for (Permission permission : permissions) {
                if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("权限不属于当前租户");
                }
            }
            
            // 分配权限
            role.setPermissions(permissions);
            roleRepository.save(role);
            
            log.info("角色 {} 权限分配成功", role.getRoleName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("分配权限失败: ", e);
            throw BusinessException.of("分配权限失败");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getRolePermissions(Long roleId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找角色
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("角色不存在"));
            
            // 验证租户权限
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限查看此角色");
            }
            
            // 获取角色的权限
            List<Permission> permissions = permissionRepository.findByRoleId(Long.valueOf(tenantId), roleId);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取角色权限失败: ", e);
            throw BusinessException.of("获取角色权限失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> assignRolesToUser(AssignRoleRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 验证租户权限
            if (!request.getTenantId().equals(tenantId)) {
                throw BusinessException.of("无权限操作此用户");
            }
            
            // 删除用户现有角色
            userRoleRepository.deleteByTenantIdAndUserId(Long.valueOf(tenantId), request.getUserId());
            
            // 分配新角色
            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                for (Long roleId : request.getRoleIds()) {
                    UserRole userRole = new UserRole();
                    userRole.setTenantId(Long.valueOf(tenantId));
                    userRole.setUserId(request.getUserId());
                    userRole.setRoleId(roleId);
                    userRoleRepository.save(userRole);
                }
            }
            
            log.info("用户 {} 角色分配成功", request.getUserId());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("分配角色失败: ", e);
            throw BusinessException.of("分配角色失败");
        }
    }
    
    @Override
    public ApiResponse<List<RoleVO>> getUserRoles(Long userId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 获取用户的角色
            List<Role> roles = roleRepository.findByUserId(Long.valueOf(tenantId), userId);
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(roleVOs);
            
        } catch (Exception e) {
            log.error("获取用户角色失败: ", e);
            throw BusinessException.of("获取用户角色失败");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getUserPermissions(Long userId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 获取用户的权限
            List<Permission> permissions = permissionRepository.findByUserId(Long.valueOf(tenantId), userId);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("获取用户权限失败: ", e);
            throw BusinessException.of("获取用户权限失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkUserPermission(Long userId, String permissionCode) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 获取用户的权限
            List<Permission> permissions = permissionRepository.findByUserId(Long.valueOf(tenantId), userId);
            
            // 检查是否有指定权限
            boolean hasPermission = permissions.stream()
                    .anyMatch(p -> p.getPermissionCode().equals(permissionCode) && "ACTIVE".equals(p.getStatus()));
            
            return ApiResponse.success(hasPermission);
            
        } catch (Exception e) {
            log.error("检查用户权限失败: ", e);
            throw BusinessException.of("检查用户权限失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkUserRole(Long userId, String roleCode) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 获取用户的角色
            List<Role> roles = roleRepository.findByUserId(Long.valueOf(tenantId), userId);
            
            // 检查是否有指定角色
            boolean hasRole = roles.stream()
                    .anyMatch(r -> r.getRoleCode().equals(roleCode) && "ACTIVE".equals(r.getStatus()));
            
            return ApiResponse.success(hasRole);
            
        } catch (Exception e) {
            log.error("检查用户角色失败: ", e);
            throw BusinessException.of("检查用户角色失败");
        }
    }
    
    /**
     * 将Role实体转换为RoleVO
     */
    private RoleVO convertToRoleVO(Role role) {
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setTenantId(role.getTenantId().toString());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        vo.setIsSystem(role.getIsSystem());
        vo.setCreatedAt(role.getCreatedAt());
        vo.setUpdatedAt(role.getUpdatedAt());
        
        // 转换权限列表
        if (role.getPermissions() != null) {
            List<PermissionVO> permissionVOs = role.getPermissions().stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            vo.setPermissions(permissionVOs);
        }
        
        return vo;
    }
    
    /**
     * 将Permission实体转换为PermissionVO
     */
    private PermissionVO convertToPermissionVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        vo.setId(permission.getId());
        vo.setTenantId(permission.getTenantId().toString());
        vo.setPermissionName(permission.getPermissionName());
        vo.setPermissionCode(permission.getPermissionCode());
        vo.setPermissionType(permission.getPermissionType());
        vo.setParentId(permission.getParentId());
        vo.setPath(permission.getPath());
        vo.setComponent(permission.getComponent());
        vo.setIcon(permission.getIcon());
        vo.setSortOrder(permission.getSortOrder());
        vo.setDescription(permission.getDescription());
        vo.setStatus(permission.getStatus());
        vo.setIsSystem(permission.getIsSystem());
        vo.setCreatedAt(permission.getCreatedAt());
        vo.setUpdatedAt(permission.getUpdatedAt());
        return vo;
    }
    
    /**
     * 构建权限树
     */
    private List<PermissionVO> buildPermissionTree(List<Permission> permissions) {
        // 转换为VO
        List<PermissionVO> permissionVOs = permissions.stream()
                .map(this::convertToPermissionVO)
                .collect(Collectors.toList());
        
        // 构建父子关系映射
        Map<Long, List<PermissionVO>> parentChildMap = permissionVOs.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(PermissionVO::getParentId));
        
        // 设置子权限
        permissionVOs.forEach(vo -> {
            List<PermissionVO> children = parentChildMap.get(vo.getId());
            if (children != null) {
                vo.setChildren(children);
            }
        });
        
        // 返回根权限
        return permissionVOs.stream()
                .filter(p -> p.getParentId() == null)
                .collect(Collectors.toList());
    }
    
    // 新增方法实现
    @Override
    public ApiResponse<Page<RoleVO>> searchRolesAdvanced(RoleSearchRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 构建分页参数
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            
            // 根据搜索条件查找角色
            Page<Role> rolePage = null;
            
            if (StringUtils.hasText(request.getRoleName())) {
                rolePage = roleRepository.findByTenantIdAndRoleNameContaining(Long.valueOf(tenantId), request.getRoleName(), pageable);
            } else if (StringUtils.hasText(request.getRoleCode())) {
                rolePage = roleRepository.findByTenantIdAndRoleCodeContaining(Long.valueOf(tenantId), request.getRoleCode(), pageable);
            } else if (StringUtils.hasText(request.getStatus())) {
                rolePage = roleRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), request.getStatus(), pageable);
            } else if (request.getIsSystem() != null) {
                rolePage = roleRepository.findByTenantIdAndIsSystem(Long.valueOf(tenantId), request.getIsSystem(), pageable);
            } else {
                // 默认查询所有角色
                rolePage = roleRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            }
            
            if (rolePage != null) {
                Page<RoleVO> roleVOPage = rolePage.map(this::convertToRoleVO);
                return ApiResponse.success(roleVOPage);
            } else {
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("高级搜索角色失败: ", e);
            throw BusinessException.of("高级搜索角色失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchEnableRoles(List<Long> roleIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量启用角色
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> BusinessException.of("角色不存在: " + roleId));
                
                // 验证租户权限
                if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作角色: " + roleId);
                }
                
                role.setStatus("ACTIVE");
                roleRepository.save(role);
            }
            
            log.info("批量启用角色成功: {}", roleIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量启用角色失败: ", e);
            throw BusinessException.of("批量启用角色失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDisableRoles(List<Long> roleIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量禁用角色
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> BusinessException.of("角色不存在: " + roleId));
                
                // 验证租户权限
                if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作角色: " + roleId);
                }
                
                role.setStatus("INACTIVE");
                roleRepository.save(role);
            }
            
            log.info("批量禁用角色成功: {}", roleIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量禁用角色失败: ", e);
            throw BusinessException.of("批量禁用角色失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeleteRoles(List<Long> roleIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量删除角色
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> BusinessException.of("角色不存在: " + roleId));
                
                // 验证租户权限
                if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作角色: " + roleId);
                }
                
                // 检查是否为系统角色
                if (role.getIsSystem()) {
                    throw BusinessException.of("系统角色不能删除: " + roleId);
                }
                
                roleRepository.delete(role);
            }
            
            log.info("批量删除角色成功: {}", roleIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除角色失败: ", e);
            throw BusinessException.of("批量删除角色失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkRoleCodeExists(String roleCode) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = roleRepository.existsByTenantIdAndRoleCode(Long.valueOf(tenantId), roleCode);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查角色代码是否存在失败: ", e);
            throw BusinessException.of("检查角色代码是否存在失败");
        }
    }
    
    @Override
    public ApiResponse<Page<PermissionVO>> searchPermissionsAdvanced(PermissionSearchRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 构建分页参数
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            
            // 根据搜索条件查找权限
            Page<Permission> permissionPage = null;
            
            if (StringUtils.hasText(request.getPermissionName())) {
                permissionPage = permissionRepository.findByTenantIdAndPermissionNameContaining(Long.valueOf(tenantId), request.getPermissionName(), pageable);
            } else if (StringUtils.hasText(request.getPermissionCode())) {
                permissionPage = permissionRepository.findByTenantIdAndPermissionCodeContaining(Long.valueOf(tenantId), request.getPermissionCode(), pageable);
            } else if (StringUtils.hasText(request.getPermissionType())) {
                permissionPage = permissionRepository.findByTenantIdAndPermissionType(Long.valueOf(tenantId), request.getPermissionType(), pageable);
            } else if (StringUtils.hasText(request.getStatus())) {
                permissionPage = permissionRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), request.getStatus(), pageable);
            } else if (request.getIsSystem() != null) {
                permissionPage = permissionRepository.findByTenantIdAndIsSystem(Long.valueOf(tenantId), request.getIsSystem(), pageable);
            } else {
                // 默认查询所有权限
                permissionPage = permissionRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            }
            
            if (permissionPage != null) {
                Page<PermissionVO> permissionVOPage = permissionPage.map(this::convertToPermissionVO);
                return ApiResponse.success(permissionVOPage);
            } else {
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("高级搜索权限失败: ", e);
            throw BusinessException.of("高级搜索权限失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchEnablePermissions(List<Long> permissionIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量启用权限
            for (Long permissionId : permissionIds) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> BusinessException.of("权限不存在: " + permissionId));
                
                // 验证租户权限
                if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作权限: " + permissionId);
                }
                
                permission.setStatus("ACTIVE");
                permissionRepository.save(permission);
            }
            
            log.info("批量启用权限成功: {}", permissionIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量启用权限失败: ", e);
            throw BusinessException.of("批量启用权限失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDisablePermissions(List<Long> permissionIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量禁用权限
            for (Long permissionId : permissionIds) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> BusinessException.of("权限不存在: " + permissionId));
                
                // 验证租户权限
                if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作权限: " + permissionId);
                }
                
                permission.setStatus("INACTIVE");
                permissionRepository.save(permission);
            }
            
            log.info("批量禁用权限成功: {}", permissionIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量禁用权限失败: ", e);
            throw BusinessException.of("批量禁用权限失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeletePermissions(List<Long> permissionIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量删除权限
            for (Long permissionId : permissionIds) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> BusinessException.of("权限不存在: " + permissionId));
                
                // 验证租户权限
                if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("无权限操作权限: " + permissionId);
                }
                
                // 检查是否为系统权限
                if (permission.getIsSystem()) {
                    throw BusinessException.of("系统权限不能删除: " + permissionId);
                }
                
                // 检查是否有子权限
                List<Permission> children = permissionRepository.findByTenantIdAndParentId(Long.valueOf(tenantId), permissionId);
                if (!children.isEmpty()) {
                    throw BusinessException.of("存在子权限，不能删除: " + permissionId);
                }
                
                permissionRepository.delete(permission);
            }
            
            log.info("批量删除权限成功: {}", permissionIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除权限失败: ", e);
            throw BusinessException.of("批量删除权限失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkPermissionCodeExists(String permissionCode) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = permissionRepository.existsByTenantIdAndPermissionCode(Long.valueOf(tenantId), permissionCode);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查权限代码是否存在失败: ", e);
            throw BusinessException.of("检查权限代码是否存在失败");
        }
    }
    
    @Override
    public ApiResponse<PermissionStatisticsVO> getPermissionStatistics() {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 获取角色统计信息
            Long totalRoles = roleRepository.countByTenantId(Long.valueOf(tenantId));
            Long activeRoles = roleRepository.countByTenantIdAndStatusActive(Long.valueOf(tenantId));
            Long inactiveRoles = roleRepository.countByTenantIdAndStatusInactive(Long.valueOf(tenantId));
            Long systemRoles = roleRepository.countByTenantIdAndIsSystemTrue(Long.valueOf(tenantId));
            Long customRoles = roleRepository.countByTenantIdAndIsSystemFalse(Long.valueOf(tenantId));
            
            // 获取权限统计信息
            Long totalPermissions = permissionRepository.countByTenantId(Long.valueOf(tenantId));
            Long activePermissions = permissionRepository.countByTenantIdAndStatusActive(Long.valueOf(tenantId));
            Long inactivePermissions = permissionRepository.countByTenantIdAndStatusInactive(Long.valueOf(tenantId));
            Long systemPermissions = permissionRepository.countByTenantIdAndIsSystemTrue(Long.valueOf(tenantId));
            Long customPermissions = permissionRepository.countByTenantIdAndIsSystemFalse(Long.valueOf(tenantId));
            Long menuPermissions = permissionRepository.countByTenantIdAndPermissionType(Long.valueOf(tenantId), "MENU");
            Long buttonPermissions = permissionRepository.countByTenantIdAndPermissionType(Long.valueOf(tenantId), "BUTTON");
            Long apiPermissions = permissionRepository.countByTenantIdAndPermissionType(Long.valueOf(tenantId), "API");
            
            // 构建统计信息
            PermissionStatisticsVO statistics = new PermissionStatisticsVO();
            statistics.setTotalRoles(totalRoles);
            statistics.setActiveRoles(activeRoles);
            statistics.setInactiveRoles(inactiveRoles);
            statistics.setSystemRoles(systemRoles);
            statistics.setCustomRoles(customRoles);
            statistics.setTotalPermissions(totalPermissions);
            statistics.setActivePermissions(activePermissions);
            statistics.setInactivePermissions(inactivePermissions);
            statistics.setSystemPermissions(systemPermissions);
            statistics.setCustomPermissions(customPermissions);
            statistics.setMenuPermissions(menuPermissions);
            statistics.setButtonPermissions(buttonPermissions);
            statistics.setApiPermissions(apiPermissions);
            
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("获取权限统计信息失败: ", e);
            throw BusinessException.of("获取权限统计信息失败");
        }
    }
} 