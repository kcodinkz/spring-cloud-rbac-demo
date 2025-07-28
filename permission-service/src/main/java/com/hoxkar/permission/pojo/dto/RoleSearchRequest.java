package com.hoxkar.permission.pojo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 角色搜索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleSearchRequest {
    
    /**
     * 角色名称（模糊查询）
     */
    private String roleName;
    
    /**
     * 角色代码（模糊查询）
     */
    private String roleCode;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 是否为系统角色
     */
    private Boolean isSystem;
    
    /**
     * 页码
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    private Integer size = 10;

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
} 