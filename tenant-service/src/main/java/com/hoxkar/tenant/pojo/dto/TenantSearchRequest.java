package com.hoxkar.tenant.pojo.dto;

import com.hoxkar.common.pojo.dto.PageRequestDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 租户搜索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantSearchRequest {
    
    /**
     * 租户名称（模糊查询）
     */
    private String tenantName;
    
    /**
     * 租户代码（模糊查询）
     */
    private String tenantCode;
    
    /**
     * 域名（模糊查询）
     */
    private String domain;
    
    /**
     * 联系邮箱（模糊查询）
     */
    private String contactEmail;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 订阅计划
     */
    private String subscriptionPlan;
    
    /**
     * 分页参数
     */
    private PageRequestDTO pageRequest = new PageRequestDTO();
} 