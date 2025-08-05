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

// Permission Service Implementation
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
    
    // Role Management
    @Override
    @Transactional
    public ApiResponse<RoleVO> createRole(CreateRoleRequest request) {
        try {
            // Set tenant context
            TenantContext.setTenantId(request.getTenantId());
            
            // Check if role code already exists
            if (roleRepository.existsByTenantIdAndRoleCode(Long.valueOf(request.getTenantId()), request.getRoleCode())) {
                throw BusinessException.of("Role code already exists");
            }
            
            // Create role entity
            Role role = new Role();
            role.setTenantId(Long.valueOf(request.getTenantId()));
            role.setRoleName(request.getRoleName());
            role.setRoleCode(request.getRoleCode());
            role.setDescription(request.getDescription());
            role.setIsSystem(request.getIsSystem());
            role.setStatus("ACTIVE");
            
            // Save role
            Role savedRole = roleRepository.save(role);
            
            // Assign permissions
            if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
                assignPermissionsToRole(savedRole.getId(), request.getPermissionIds());
            }
            
            log.info("Role created successfully: {}", savedRole.getRoleName());
            return ApiResponse.success("Role created successfully", convertToRoleVO(savedRole));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create role: ", e);
            throw BusinessException.of("Failed to create role");
        } finally {
            TenantContext.clear();
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<RoleVO> updateRole(Long roleId, UpdateRoleRequest request) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("Role not found"));
            
            // Validate tenant permission
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this role");
            }
            
            // Check if role code is already used by another role
            if (request.getRoleCode() != null && !request.getRoleCode().equals(role.getRoleCode())) {
                if (roleRepository.existsByTenantIdAndRoleCode(Long.valueOf(tenantId), request.getRoleCode())) {
                    throw BusinessException.of("Role code is already used by another role");
                }
                role.setRoleCode(request.getRoleCode());
            }
            
            // Update role information
            if (request.getRoleName() != null) {
                role.setRoleName(request.getRoleName());
            }
            if (request.getDescription() != null) {
                role.setDescription(request.getDescription());
            }
            if (request.getStatus() != null) {
                role.setStatus(request.getStatus());
            }
            
            // Save role
            Role updatedRole = roleRepository.save(role);
            
            // Update permission assignment
            if (request.getPermissionIds() != null) {
                assignPermissionsToRole(roleId, request.getPermissionIds());
            }
            
            log.info("Role updated successfully: {}", updatedRole.getRoleName());
            return ApiResponse.success("Role updated successfully", convertToRoleVO(updatedRole));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update role: ", e);
            throw BusinessException.of("Failed to update role");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteRole(Long roleId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("Role not found"));
            
            // Validate tenant permission
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this role");
            }
            
            // Check if it's a system role
            if (role.getIsSystem()) {
                throw BusinessException.of("System roles cannot be deleted");
            }
            
            // Delete role
            roleRepository.delete(role);
            
            log.info("Role {} deleted successfully", role.getRoleName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete role: ", e);
            throw BusinessException.of("Failed to delete role");
        }
    }
    
    @Override
    public ApiResponse<RoleVO> getRoleById(Long roleId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("Role not found"));
            
            // Validate tenant permission
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to view this role");
            }
            
            return ApiResponse.success(convertToRoleVO(role));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get role: ", e);
            throw BusinessException.of("Failed to get role");
        }
    }
    
    @Override
    public ApiResponse<RoleVO> getRoleByCode(String roleCode) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find role
            Role role = roleRepository.findByTenantIdAndRoleCode(Long.valueOf(tenantId), roleCode)
                    .orElseThrow(() -> BusinessException.of("Role not found"));
            
            return ApiResponse.success(convertToRoleVO(role));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get role: ", e);
            throw BusinessException.of("Failed to get role");
        }
    }
    
    @Override
    public ApiResponse<List<RoleVO>> getAllRoles() {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find all roles
            List<Role> roles = roleRepository.findAllByTenantId(Long.valueOf(tenantId));
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(roleVOs);
            
        } catch (Exception e) {
            log.error("Failed to get role list: ", e);
            throw BusinessException.of("Failed to get role list");
        }
    }
    
    @Override
    public ApiResponse<Page<RoleVO>> getRolesByPage(Pageable pageable) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Paginate roles
            Page<Role> rolePage = roleRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            Page<RoleVO> roleVOPage = rolePage.map(this::convertToRoleVO);
            
            return ApiResponse.success(roleVOPage);
            
        } catch (Exception e) {
            log.error("Failed to paginate roles: ", e);
            throw BusinessException.of("Failed to paginate roles");
        }
    }
    
    @Override
    public ApiResponse<List<RoleVO>> getRolesByStatus(String status) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find roles by status
            List<Role> roles = roleRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), status);
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(roleVOs);
            
        } catch (Exception e) {
            log.error("Failed to get roles by status: ", e);
            throw BusinessException.of("Failed to get roles by status");
        }
    }
    
    @Override
    public ApiResponse<List<RoleVO>> searchRoles(String roleName) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Search roles by role name
            List<Role> roles = roleRepository.findByTenantIdAndRoleNameContaining(Long.valueOf(tenantId), roleName);
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(roleVOs);
            
        } catch (Exception e) {
            log.error("Failed to search roles: ", e);
            throw BusinessException.of("Failed to search roles");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> enableRole(Long roleId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("Role not found"));
            
            // Validate tenant permission
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this role");
            }
            
            // Enable role
            role.setStatus("ACTIVE");
            roleRepository.save(role);
            
            log.info("Role {} enabled successfully", role.getRoleName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to enable role: ", e);
            throw BusinessException.of("Failed to enable role");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> disableRole(Long roleId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("Role not found"));
            
            // Validate tenant permission
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this role");
            }
            
            // Disable role
            role.setStatus("INACTIVE");
            roleRepository.save(role);
            
            log.info("Role {} disabled successfully", role.getRoleName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to disable role: ", e);
            throw BusinessException.of("Failed to disable role");
        }
    }
    
    // Permission Management
    @Override
    @Transactional
    public ApiResponse<PermissionVO> createPermission(CreatePermissionRequest request) {
        try {
            // Set tenant context
            TenantContext.setTenantId(request.getTenantId());
            
            // Check if permission code already exists
            if (permissionRepository.existsByTenantIdAndPermissionCode(Long.valueOf(request.getTenantId()), request.getPermissionCode())) {
                throw BusinessException.of("Permission code already exists");
            }
            
            // Create permission entity
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
            
            // Save permission
            Permission savedPermission = permissionRepository.save(permission);
            
            log.info("Permission created successfully: {}", savedPermission.getPermissionName());
            return ApiResponse.success("Permission created successfully", convertToPermissionVO(savedPermission));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create permission: ", e);
            throw BusinessException.of("Failed to create permission");
        } finally {
            TenantContext.clear();
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<PermissionVO> updatePermission(Long permissionId, UpdatePermissionRequest request) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find permission
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("Permission not found"));
            
            // Validate tenant permission
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this permission");
            }
            
            // Check if permission code is already used by another permission
            if (request.getPermissionCode() != null && !request.getPermissionCode().equals(permission.getPermissionCode())) {
                if (permissionRepository.existsByTenantIdAndPermissionCode(Long.valueOf(tenantId), request.getPermissionCode())) {
                    throw BusinessException.of("Permission code is already used by another permission");
                }
                permission.setPermissionCode(request.getPermissionCode());
            }
            
            // Update permission information
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
            
            // Save permission
            Permission updatedPermission = permissionRepository.save(permission);
            
            log.info("Permission updated successfully: {}", updatedPermission.getPermissionName());
            return ApiResponse.success("Permission updated successfully", convertToPermissionVO(updatedPermission));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update permission: ", e);
            throw BusinessException.of("Failed to update permission");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deletePermission(Long permissionId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find permission
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("Permission not found"));
            
            // Validate tenant permission
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this permission");
            }
            
            // Check if it's a system permission
            if (permission.getIsSystem()) {
                throw BusinessException.of("System permissions cannot be deleted");
            }
            
            // Check if there are child permissions
            List<Permission> children = permissionRepository.findByTenantIdAndParentId(Long.valueOf(tenantId), permissionId);
            if (!children.isEmpty()) {
                throw BusinessException.of("Child permissions exist, cannot delete");
            }
            
            // Delete permission
            permissionRepository.delete(permission);
            
            log.info("Permission {} deleted successfully", permission.getPermissionName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete permission: ", e);
            throw BusinessException.of("Failed to delete permission");
        }
    }
    
    @Override
    public ApiResponse<PermissionVO> getPermissionById(Long permissionId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find permission
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("Permission not found"));
            
            // Validate tenant permission
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to view this permission");
            }
            
            return ApiResponse.success(convertToPermissionVO(permission));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get permission: ", e);
            throw BusinessException.of("Failed to get permission");
        }
    }
    
    @Override
    public ApiResponse<PermissionVO> getPermissionByCode(String permissionCode) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find permission
            Permission permission = permissionRepository.findByTenantIdAndPermissionCode(Long.valueOf(tenantId), permissionCode)
                    .orElseThrow(() -> BusinessException.of("Permission not found"));
            
            return ApiResponse.success(convertToPermissionVO(permission));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get permission: ", e);
            throw BusinessException.of("Failed to get permission");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getAllPermissions() {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find all permissions
            List<Permission> permissions = permissionRepository.findAllByTenantId(Long.valueOf(tenantId));
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("Failed to get permission list: ", e);
            throw BusinessException.of("Failed to get permission list");
        }
    }
    
    @Override
    public ApiResponse<Page<PermissionVO>> getPermissionsByPage(Pageable pageable) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Paginate permissions
            Page<Permission> permissionPage = permissionRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            Page<PermissionVO> permissionVOPage = permissionPage.map(this::convertToPermissionVO);
            
            return ApiResponse.success(permissionVOPage);
            
        } catch (Exception e) {
            log.error("Failed to paginate permissions: ", e);
            throw BusinessException.of("Failed to paginate permissions");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getPermissionsByType(String permissionType) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find permissions by permission type
            List<Permission> permissions = permissionRepository.findByTenantIdAndPermissionType(Long.valueOf(tenantId), permissionType);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("Failed to get permissions by type: ", e);
            throw BusinessException.of("Failed to get permissions by type");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getPermissionTree() {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find all permissions
            List<Permission> permissions = permissionRepository.findAllByTenantId(Long.valueOf(tenantId));
            
            // Build permission tree
            List<PermissionVO> permissionTree = buildPermissionTree(permissions);
            
            return ApiResponse.success(permissionTree);
            
        } catch (Exception e) {
            log.error("Failed to get permission tree: ", e);
            throw BusinessException.of("Failed to get permission tree");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getPermissionsByStatus(String status) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find permissions by status
            List<Permission> permissions = permissionRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), status);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("Failed to get permissions by status: ", e);
            throw BusinessException.of("Failed to get permissions by status");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> searchPermissions(String permissionName) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Search permissions by permission name
            List<Permission> permissions = permissionRepository.findByTenantIdAndPermissionNameContaining(Long.valueOf(tenantId), permissionName);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("Failed to search permissions: ", e);
            throw BusinessException.of("Failed to search permissions");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> enablePermission(Long permissionId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find permission
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("Permission not found"));
            
            // Validate tenant permission
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this permission");
            }
            
            // Enable permission
            permission.setStatus("ACTIVE");
            permissionRepository.save(permission);
            
            log.info("Permission {} enabled successfully", permission.getPermissionName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to enable permission: ", e);
            throw BusinessException.of("Failed to enable permission");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> disablePermission(Long permissionId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find permission
            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> BusinessException.of("Permission not found"));
            
            // Validate tenant permission
            if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this permission");
            }
            
            // Disable permission
            permission.setStatus("INACTIVE");
            permissionRepository.save(permission);
            
            log.info("Permission {} disabled successfully", permission.getPermissionName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to disable permission: ", e);
            throw BusinessException.of("Failed to disable permission");
        }
    }
    
    // Role Permission Assignment
    @Override
    @Transactional
    public ApiResponse<Void> assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("Role not found"));
            
            // Validate tenant permission
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to operate this role");
            }
            
            // Find permissions
            List<Permission> permissions = permissionRepository.findAllById(permissionIds);
            
            // Validate permissions belong to the same tenant
            for (Permission permission : permissions) {
                if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("Permission does not belong to the current tenant");
                }
            }
            
            // Assign permissions
            role.setPermissions(permissions);
            roleRepository.save(role);
            
            log.info("Permissions assigned to role {} successfully", role.getRoleName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to assign permissions: ", e);
            throw BusinessException.of("Failed to assign permissions");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getRolePermissions(Long roleId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find role
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> BusinessException.of("Role not found"));
            
            // Validate tenant permission
            if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to view this role");
            }
            
            // Get role permissions
            List<Permission> permissions = permissionRepository.findByRoleId(Long.valueOf(tenantId), roleId);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get role permissions: ", e);
            throw BusinessException.of("Failed to get role permissions");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> assignRolesToUser(AssignRoleRequest request) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Validate tenant permission
            if (!request.getTenantId().equals(tenantId)) {
                throw BusinessException.of("No permission to operate this user");
            }
            
            // Delete existing user roles
            userRoleRepository.deleteByTenantIdAndUserId(Long.valueOf(tenantId), request.getUserId());
            
            // Assign new roles
            if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
                for (Long roleId : request.getRoleIds()) {
                    UserRole userRole = new UserRole();
                    userRole.setTenantId(Long.valueOf(tenantId));
                    userRole.setUserId(request.getUserId());
                    userRole.setRoleId(roleId);
                    userRoleRepository.save(userRole);
                }
            }
            
            log.info("User {} roles assigned successfully", request.getUserId());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to assign roles: ", e);
            throw BusinessException.of("Failed to assign roles");
        }
    }
    
    @Override
    public ApiResponse<List<RoleVO>> getUserRoles(Long userId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Get user roles
            List<Role> roles = roleRepository.findByUserId(Long.valueOf(tenantId), userId);
            List<RoleVO> roleVOs = roles.stream()
                    .map(this::convertToRoleVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(roleVOs);
            
        } catch (Exception e) {
            log.error("Failed to get user roles: ", e);
            throw BusinessException.of("Failed to get user roles");
        }
    }
    
    @Override
    public ApiResponse<List<PermissionVO>> getUserPermissions(Long userId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Get user permissions
            List<Permission> permissions = permissionRepository.findByUserId(Long.valueOf(tenantId), userId);
            List<PermissionVO> permissionVOs = permissions.stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(permissionVOs);
            
        } catch (Exception e) {
            log.error("Failed to get user permissions: ", e);
            throw BusinessException.of("Failed to get user permissions");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkUserPermission(Long userId, String permissionCode) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Get user permissions
            List<Permission> permissions = permissionRepository.findByUserId(Long.valueOf(tenantId), userId);
            
            // Check if user has the specified permission
            boolean hasPermission = permissions.stream()
                    .anyMatch(p -> p.getPermissionCode().equals(permissionCode) && "ACTIVE".equals(p.getStatus()));
            
            return ApiResponse.success(hasPermission);
            
        } catch (Exception e) {
            log.error("Failed to check user permission: ", e);
            throw BusinessException.of("Failed to check user permission");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkUserRole(Long userId, String roleCode) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Get user roles
            List<Role> roles = roleRepository.findByUserId(Long.valueOf(tenantId), userId);
            
            // Check if user has the specified role
            boolean hasRole = roles.stream()
                    .anyMatch(r -> r.getRoleCode().equals(roleCode) && "ACTIVE".equals(r.getStatus()));
            
            return ApiResponse.success(hasRole);
            
        } catch (Exception e) {
            log.error("Failed to check user role: ", e);
            throw BusinessException.of("Failed to check user role");
        }
    }
    
    /**
     * Convert Role entity to RoleVO
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
        
        // Convert permission list
        if (role.getPermissions() != null) {
            List<PermissionVO> permissionVOs = role.getPermissions().stream()
                    .map(this::convertToPermissionVO)
                    .collect(Collectors.toList());
            vo.setPermissions(permissionVOs);
        }
        
        return vo;
    }
    
    /**
     * Convert Permission entity to PermissionVO
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
     * Build permission tree
     */
    private List<PermissionVO> buildPermissionTree(List<Permission> permissions) {
        // Convert to VO
        List<PermissionVO> permissionVOs = permissions.stream()
                .map(this::convertToPermissionVO)
                .collect(Collectors.toList());
        
        // Build parent-child relationship map
        Map<Long, List<PermissionVO>> parentChildMap = permissionVOs.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(PermissionVO::getParentId));
        
        // Set child permissions
        permissionVOs.forEach(vo -> {
            List<PermissionVO> children = parentChildMap.get(vo.getId());
            if (children != null) {
                vo.setChildren(children);
            }
        });
        
        // Return root permissions
        return permissionVOs.stream()
                .filter(p -> p.getParentId() == null)
                .collect(Collectors.toList());
    }
    
    // New method implementation
    @Override
    public ApiResponse<Page<RoleVO>> searchRolesAdvanced(RoleSearchRequest request) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Build pagination parameters
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            
            // Find roles based on search criteria
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
                // Default query all roles
                rolePage = roleRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            }
            
            if (rolePage != null) {
                Page<RoleVO> roleVOPage = rolePage.map(this::convertToRoleVO);
                return ApiResponse.success(roleVOPage);
            } else {
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("Advanced role search failed: ", e);
            throw BusinessException.of("Advanced role search failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchEnableRoles(List<Long> roleIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch enable roles
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> BusinessException.of("Role not found: " + roleId));
                
                // Validate tenant permission
                if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate role: " + roleId);
                }
                
                role.setStatus("ACTIVE");
                roleRepository.save(role);
            }
            
            log.info("Batch enable roles successful: {}", roleIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch enable roles failed: ", e);
            throw BusinessException.of("Batch enable roles failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDisableRoles(List<Long> roleIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch disable roles
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> BusinessException.of("Role not found: " + roleId));
                
                // Validate tenant permission
                if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate role: " + roleId);
                }
                
                role.setStatus("INACTIVE");
                roleRepository.save(role);
            }
            
            log.info("Batch disable roles successful: {}", roleIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch disable roles failed: ", e);
            throw BusinessException.of("Batch disable roles failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeleteRoles(List<Long> roleIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch delete roles
            for (Long roleId : roleIds) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> BusinessException.of("Role not found: " + roleId));
                
                // Validate tenant permission
                if (!role.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate role: " + roleId);
                }
                
                // Check if it's a system role
                if (role.getIsSystem()) {
                    throw BusinessException.of("System roles cannot be deleted: " + roleId);
                }
                
                roleRepository.delete(role);
            }
            
            log.info("Batch delete roles successful: {}", roleIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch delete roles failed: ", e);
            throw BusinessException.of("Batch delete roles failed");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkRoleCodeExists(String roleCode) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = roleRepository.existsByTenantIdAndRoleCode(Long.valueOf(tenantId), roleCode);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("Failed to check if role code exists: ", e);
            throw BusinessException.of("Failed to check if role code exists");
        }
    }
    
    @Override
    public ApiResponse<Page<PermissionVO>> searchPermissionsAdvanced(PermissionSearchRequest request) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Build pagination parameters
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            
            // Find permissions based on search criteria
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
                // Default query all permissions
                permissionPage = permissionRepository.findAllByTenantId(Long.valueOf(tenantId), pageable);
            }
            
            if (permissionPage != null) {
                Page<PermissionVO> permissionVOPage = permissionPage.map(this::convertToPermissionVO);
                return ApiResponse.success(permissionVOPage);
            } else {
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("Advanced permission search failed: ", e);
            throw BusinessException.of("Advanced permission search failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchEnablePermissions(List<Long> permissionIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch enable permissions
            for (Long permissionId : permissionIds) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> BusinessException.of("Permission not found: " + permissionId));
                
                // Validate tenant permission
                if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate permission: " + permissionId);
                }
                
                permission.setStatus("ACTIVE");
                permissionRepository.save(permission);
            }
            
            log.info("Batch enable permissions successful: {}", permissionIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch enable permissions failed: ", e);
            throw BusinessException.of("Batch enable permissions failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDisablePermissions(List<Long> permissionIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch disable permissions
            for (Long permissionId : permissionIds) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> BusinessException.of("Permission not found: " + permissionId));
                
                // Validate tenant permission
                if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate permission: " + permissionId);
                }
                
                permission.setStatus("INACTIVE");
                permissionRepository.save(permission);
            }
            
            log.info("Batch disable permissions successful: {}", permissionIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch disable permissions failed: ", e);
            throw BusinessException.of("Batch disable permissions failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeletePermissions(List<Long> permissionIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch delete permissions
            for (Long permissionId : permissionIds) {
                Permission permission = permissionRepository.findById(permissionId)
                        .orElseThrow(() -> BusinessException.of("Permission not found: " + permissionId));
                
                // Validate tenant permission
                if (!permission.getTenantId().equals(Long.valueOf(tenantId))) {
                    throw BusinessException.of("No permission to operate permission: " + permissionId);
                }
                
                // Check if it's a system permission
                if (permission.getIsSystem()) {
                    throw BusinessException.of("System permissions cannot be deleted: " + permissionId);
                }
                
                // Check if there are child permissions
                List<Permission> children = permissionRepository.findByTenantIdAndParentId(Long.valueOf(tenantId), permissionId);
                if (!children.isEmpty()) {
                    throw BusinessException.of("Child permissions exist, cannot delete: " + permissionId);
                }
                
                permissionRepository.delete(permission);
            }
            
            log.info("Batch delete permissions successful: {}", permissionIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch delete permissions failed: ", e);
            throw BusinessException.of("Batch delete permissions failed");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkPermissionCodeExists(String permissionCode) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = permissionRepository.existsByTenantIdAndPermissionCode(Long.valueOf(tenantId), permissionCode);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("Failed to check if permission code exists: ", e);
            throw BusinessException.of("Failed to check if permission code exists");
        }
    }
    
    @Override
    public ApiResponse<PermissionStatisticsVO> getPermissionStatistics() {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Get role statistics
            Long totalRoles = roleRepository.countByTenantId(Long.valueOf(tenantId));
            Long activeRoles = roleRepository.countByTenantIdAndStatusActive(Long.valueOf(tenantId));
            Long inactiveRoles = roleRepository.countByTenantIdAndStatusInactive(Long.valueOf(tenantId));
            Long systemRoles = roleRepository.countByTenantIdAndIsSystemTrue(Long.valueOf(tenantId));
            Long customRoles = roleRepository.countByTenantIdAndIsSystemFalse(Long.valueOf(tenantId));
            
            // Get permission statistics
            Long totalPermissions = permissionRepository.countByTenantId(Long.valueOf(tenantId));
            Long activePermissions = permissionRepository.countByTenantIdAndStatusActive(Long.valueOf(tenantId));
            Long inactivePermissions = permissionRepository.countByTenantIdAndStatusInactive(Long.valueOf(tenantId));
            Long systemPermissions = permissionRepository.countByTenantIdAndIsSystemTrue(Long.valueOf(tenantId));
            Long customPermissions = permissionRepository.countByTenantIdAndIsSystemFalse(Long.valueOf(tenantId));
            Long menuPermissions = permissionRepository.countByTenantIdAndPermissionType(Long.valueOf(tenantId), "MENU");
            Long buttonPermissions = permissionRepository.countByTenantIdAndPermissionType(Long.valueOf(tenantId), "BUTTON");
            Long apiPermissions = permissionRepository.countByTenantIdAndPermissionType(Long.valueOf(tenantId), "API");
            
            // Build statistics
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
            log.error("Failed to get permission statistics: ", e);
            throw BusinessException.of("Failed to get permission statistics");
        }
    }
} 