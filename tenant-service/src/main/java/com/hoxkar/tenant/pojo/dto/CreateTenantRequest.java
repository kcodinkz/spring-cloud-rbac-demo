package com.hoxkar.tenant.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

// Create Tenant Request DTO
/**
 * 创建租户请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {
    
    /**
     * Tenant code // 租户代码
     */
    @NotBlank(message = "Tenant code cannot be empty") // 租户代码不能为空
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Tenant code can only contain letters, numbers, underscore, and hyphen") // 租户代码只能包含字母、数字、下划线和连字符
    @Size(min = 3, max = 50, message = "Tenant code length must be between 3 and 50 characters") // 租户代码长度必须在3-50个字符之间
    private String tenantCode;
    
    /**
     * Tenant name // 租户名称
     */
    @NotBlank(message = "Tenant name cannot be empty") // 租户名称不能为空
    @Size(max = 100, message = "Tenant name length cannot exceed 100 characters") // 租户名称长度不能超过100个字符
    private String tenantName;
    
    /**
     * Tenant description // 租户描述
     */
    @Size(max = 500, message = "Tenant description length cannot exceed 500 characters") // 租户描述长度不能超过500个字符
    private String description;
    
    /**
     * Contact person // 联系人
     */
    @Size(max = 50, message = "Contact person length cannot exceed 50 characters") // 联系人长度不能超过50个字符
    private String contactPerson;
    
    /**
     * Contact email // 联系邮箱
     */
    @Email(message = "Invalid contact email format") // 联系邮箱格式不正确
    private String contactEmail;
    
    /**
     * Contact phone // 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid contact phone format") // 联系电话格式不正确
    private String contactPhone;
    
    /**
     * Address // 地址
     */
    @Size(max = 200, message = "Address length cannot exceed 200 characters") // 地址长度不能超过200个字符
    private String address;
    
    /**
     * Domain // 域名
     */
    @Pattern(regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid domain format") // 域名格式不正确
    private String domain;
    
    /**
     * Logo URL
     */
    @Size(max = 200, message = "Logo URL length cannot exceed 200 characters") // Logo URL长度不能超过200个字符
    private String logoUrl;
    
    /**
     * Max users // 最大用户数
     */
    private Integer maxUsers;
    
    /**
     * Subscription plan // 订阅计划
     */
    private String subscriptionPlan;
    
    /**
     * Subscription start date // 订阅开始日期
     */
    private LocalDateTime subscriptionStartDate;
    
    /**
     * Subscription end date // 订阅结束日期
     */
    private LocalDateTime subscriptionEndDate;
} 