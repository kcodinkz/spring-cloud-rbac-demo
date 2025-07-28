package com.hoxkar.permission.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 权限统计信息VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionStatisticsVO {
    
    /**
     * 总角色数
     */
    private Long totalRoles;
    
    /**
     * 活跃角色数
     */
    private Long activeRoles;
    
    /**
     * 禁用角色数
     */
    private Long inactiveRoles;
    
    /**
     * 系统角色数
     */
    private Long systemRoles;
    
    /**
     * 自定义角色数
     */
    private Long customRoles;
    
    /**
     * 总权限数
     */
    private Long totalPermissions;
    
    /**
     * 活跃权限数
     */
    private Long activePermissions;
    
    /**
     * 禁用权限数
     */
    private Long inactivePermissions;
    
    /**
     * 系统权限数
     */
    private Long systemPermissions;
    
    /**
     * 自定义权限数
     */
    private Long customPermissions;
    
    /**
     * 菜单权限数
     */
    private Long menuPermissions;
    
    /**
     * 按钮权限数
     */
    private Long buttonPermissions;
    
    /**
     * API权限数
     */
    private Long apiPermissions;

    public Long getTotalRoles() { return totalRoles; }
    public void setTotalRoles(Long totalRoles) { this.totalRoles = totalRoles; }
    public Long getActiveRoles() { return activeRoles; }
    public void setActiveRoles(Long activeRoles) { this.activeRoles = activeRoles; }
    public Long getInactiveRoles() { return inactiveRoles; }
    public void setInactiveRoles(Long inactiveRoles) { this.inactiveRoles = inactiveRoles; }
    public Long getSystemRoles() { return systemRoles; }
    public void setSystemRoles(Long systemRoles) { this.systemRoles = systemRoles; }
    public Long getCustomRoles() { return customRoles; }
    public void setCustomRoles(Long customRoles) { this.customRoles = customRoles; }
    public Long getTotalPermissions() { return totalPermissions; }
    public void setTotalPermissions(Long totalPermissions) { this.totalPermissions = totalPermissions; }
    public Long getActivePermissions() { return activePermissions; }
    public void setActivePermissions(Long activePermissions) { this.activePermissions = activePermissions; }
    public Long getInactivePermissions() { return inactivePermissions; }
    public void setInactivePermissions(Long inactivePermissions) { this.inactivePermissions = inactivePermissions; }
    public Long getSystemPermissions() { return systemPermissions; }
    public void setSystemPermissions(Long systemPermissions) { this.systemPermissions = systemPermissions; }
    public Long getCustomPermissions() { return customPermissions; }
    public void setCustomPermissions(Long customPermissions) { this.customPermissions = customPermissions; }
    public Long getMenuPermissions() { return menuPermissions; }
    public void setMenuPermissions(Long menuPermissions) { this.menuPermissions = menuPermissions; }
    public Long getButtonPermissions() { return buttonPermissions; }
    public void setButtonPermissions(Long buttonPermissions) { this.buttonPermissions = buttonPermissions; }
    public Long getApiPermissions() { return apiPermissions; }
    public void setApiPermissions(Long apiPermissions) { this.apiPermissions = apiPermissions; }
} 