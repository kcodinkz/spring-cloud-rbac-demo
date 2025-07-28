package com.hoxkar.permission.pojo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 更新权限请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionRequest {
    
    /**
     * 权限名称
     */
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permissionName;
    
    /**
     * 权限代码
     */
    @Pattern(regexp = "^[A-Z_]+$", message = "权限代码只能包含大写字母和下划线")
    @Size(max = 50, message = "权限代码长度不能超过50个字符")
    private String permissionCode;
    
    /**
     * 权限类型
     */
    @Pattern(regexp = "^(MENU|BUTTON|API)$", message = "权限类型只能是MENU、BUTTON或API")
    private String permissionType;
    
    /**
     * 父级权限ID
     */
    private Long parentId;
    
    /**
     * 路径
     */
    @Size(max = 200, message = "路径长度不能超过200个字符")
    private String path;
    
    /**
     * 组件
     */
    @Size(max = 200, message = "组件长度不能超过200个字符")
    private String component;
    
    /**
     * 图标
     */
    @Size(max = 50, message = "图标长度不能超过50个字符")
    private String icon;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 权限描述
     */
    @Size(max = 200, message = "权限描述长度不能超过200个字符")
    private String description;
    
    /**
     * 状态
     */
    private String status;

    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getPermissionType() { return permissionType; }
    public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 