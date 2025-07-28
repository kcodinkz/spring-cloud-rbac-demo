package com.hoxkar.tenant.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建租户请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {
    
    /**
     * 租户代码
     */
    @NotBlank(message = "租户代码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "租户代码只能包含字母、数字、下划线和连字符")
    @Size(min = 3, max = 50, message = "租户代码长度必须在3-50个字符之间")
    private String tenantCode;
    
    /**
     * 租户名称
     */
    @NotBlank(message = "租户名称不能为空")
    @Size(max = 100, message = "租户名称长度不能超过100个字符")
    private String tenantName;
    
    /**
     * 租户描述
     */
    @Size(max = 500, message = "租户描述长度不能超过500个字符")
    private String description;
    
    /**
     * 联系人
     */
    @Size(max = 50, message = "联系人长度不能超过50个字符")
    private String contactPerson;
    
    /**
     * 联系邮箱
     */
    @Email(message = "联系邮箱格式不正确")
    private String contactEmail;
    
    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;
    
    /**
     * 地址
     */
    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;
    
    /**
     * 域名
     */
    @Pattern(regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "域名格式不正确")
    private String domain;
    
    /**
     * Logo URL
     */
    @Size(max = 200, message = "Logo URL长度不能超过200个字符")
    private String logoUrl;
    
    /**
     * 最大用户数
     */
    private Integer maxUsers;
    
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
} 