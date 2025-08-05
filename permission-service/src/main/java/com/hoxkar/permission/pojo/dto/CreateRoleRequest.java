package com.hoxkar.permission.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

// Create Role Request DTO
/**
 * 创建角色请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {
    
    /**
     * Tenant ID // 租户ID
     */
    @NotBlank(message = "Tenant ID cannot be empty") // 租户ID不能为空
    private String tenantId;
    
    /**
     * Role name // 角色名称
     */
    @NotBlank(message = "Role name cannot be empty") // 角色名称不能为空
    @Size(max = 50, message = "Role name length cannot exceed 50 characters") // 角色名称长度不能超过50个字符
    private String roleName;
    
    /**
     * Role code // 角色代码
     */
    @NotBlank(message = "Role code cannot be empty") // 角色代码不能为空
    @Pattern(regexp = "^[A-Z_]+$", message = "Role code can only contain uppercase letters and underscores") // 角色代码只能包含大写字母和下划线
    @Size(max = 50, message = "Role code length cannot exceed 50 characters") // 角色代码长度不能超过50个字符
    private String roleCode;
    
    /**
     * Role description // 角色描述
     */
    @Size(max = 200, message = "Role description length cannot exceed 200 characters") // 角色描述长度不能超过200个字符
    private String description;
    
    /**
     * Is system role // 是否为系统角色
     */
    private Boolean isSystem = false;
    
    /**
     * Permission ID list // 权限ID列表
     */
    private java.util.List<Long> permissionIds;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }
    public java.util.List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(java.util.List<Long> permissionIds) { this.permissionIds = permissionIds; }
} 