package com.hoxkar.tenant.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantVO {
    
    /**
     * 租户ID
     */
    private Long id;
    
    /**
     * 租户代码
     */
    private String tenantCode;
    
    /**
     * 租户名称
     */
    private String tenantName;
    
    /**
     * 租户描述
     */
    private String description;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 域名
     */
    private String domain;
    
    /**
     * Logo URL
     */
    private String logoUrl;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 最大用户数
     */
    private Integer maxUsers;
    
    /**
     * 当前用户数
     */
    private Integer currentUsers;
    
    /**
     * 订阅计划
     */
    private String subscriptionPlan;
    
    /**
     * 订阅开始日期
     */
    private LocalDateTime subscriptionStartDate;
    
    /**
     * 订阅结束日期
     */
    private LocalDateTime subscriptionEndDate;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 