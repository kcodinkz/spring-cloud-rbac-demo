package com.hoxkar.permission.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 分配角色请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequest {
    
    /**
     * 租户ID
     */
    @NotNull(message = "租户ID不能为空")
    private String tenantId;
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public java.util.List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(java.util.List<Long> roleIds) { this.roleIds = roleIds; }
} 