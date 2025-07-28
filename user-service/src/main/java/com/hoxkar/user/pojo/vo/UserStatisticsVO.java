package com.hoxkar.user.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户统计信息VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsVO {
    
    /**
     * 总用户数
     */
    private Long totalUsers;
    
    /**
     * 活跃用户数
     */
    private Long activeUsers;
    
    /**
     * 禁用用户数
     */
    private Long inactiveUsers;
    
    /**
     * 超级管理员数
     */
    private Long superAdminUsers;
    
    /**
     * 普通用户数
     */
    private Long normalUsers;
    
    /**
     * 本月新增用户数
     */
    private Long newUsersThisMonth;
    
    /**
     * 本月活跃用户数
     */
    private Long activeUsersThisMonth;
} 