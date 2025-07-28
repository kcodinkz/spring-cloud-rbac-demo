package com.hoxkar.permission.pojo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 权限搜索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionSearchRequest {
    
    /**
     * 权限名称（模糊查询）
     */
    private String permissionName;
    
    /**
     * 权限代码（模糊查询）
     */
    private String permissionCode;
    
    /**
     * 权限类型
     */
    private String permissionType;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 是否为系统权限
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

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getPermissionType() { return permissionType; }
    public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsSystem() { return isSystem; }
    public void setIsSystem(Boolean isSystem) { this.isSystem = isSystem; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
} 