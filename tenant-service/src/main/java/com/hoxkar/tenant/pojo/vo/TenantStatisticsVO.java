package com.hoxkar.tenant.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * 租户统计信息VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantStatisticsVO {
    
    /**
     * 总租户数
     */
    private Long totalTenants;
    
    /**
     * 活跃租户数
     */
    private Long activeTenants;
    
    /**
     * 禁用租户数
     */
    private Long inactiveTenants;
    
    /**
     * 即将过期租户数（30天内）
     */
    private Long expiringTenants;
    
    /**
     * 已过期租户数
     */
    private Long expiredTenants;
    
    /**
     * 各状态租户数量统计
     */
    private Map<String, Long> statusStats;
    
    /**
     * 各订阅计划租户数量统计
     */
    private Map<String, Long> planStats;
    
    /**
     * 总用户数
     */
    private Long totalUsers;
    
    /**
     * 平均每租户用户数
     */
    private Double averageUsersPerTenant;
    
    /**
     * 本月新增租户数
     */
    private Long newTenantsThisMonth;
    
    /**
     * 本月活跃租户数
     */
    private Long activeTenantsThisMonth;
} 