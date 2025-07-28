package com.hoxkar.permission.pojo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 更新角色请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoleRequest {
    
    /**
     * 角色名称
     */
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;
    
    /**
     * 角色代码
     */
    @Pattern(regexp = "^[A-Z_]+$", message = "角色代码只能包含大写字母和下划线")
    @Size(max = 50, message = "角色代码长度不能超过50个字符")
    private String roleCode;
    
    /**
     * 角色描述
     */
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    private String description;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 权限ID列表
     */
    private List<Long> permissionIds;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public java.util.List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(java.util.List<Long> permissionIds) { this.permissionIds = permissionIds; }
} 