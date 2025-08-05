package com.hoxkar.tenant.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Create Tenant Config Request DTO
/**
 * 创建租户配置请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantConfigRequest {
    
    /**
     * Tenant ID // 租户ID
     */
    @NotBlank(message = "Tenant ID cannot be empty") // 租户ID不能为空
    private String tenantId;
    
    /**
     * Config key // 配置键
     */
    @NotBlank(message = "Config key cannot be empty") // 配置键不能为空
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Config key can only contain letters, numbers, dot, underscore, and hyphen") // 配置键只能包含字母、数字、点、下划线和连字符
    @Size(max = 100, message = "Config key length cannot exceed 100 characters") // 配置键长度不能超过100个字符
    private String configKey;
    
    /**
     * Config value // 配置值
     */
    private String configValue;
    
    /**
     * Config type // 配置类型
     */
    @Pattern(regexp = "^(STRING|NUMBER|BOOLEAN|JSON)$", message = "Config type must be STRING, NUMBER, BOOLEAN, or JSON") // 配置类型只能是STRING、NUMBER、BOOLEAN或JSON
    private String configType = "STRING";
    
    /**
     * Config description // 配置描述
     */
    @Size(max = 200, message = "Config description length cannot exceed 200 characters") // 配置描述长度不能超过200个字符
    private String description;
    
    /**
     * Is system config // 是否为系统配置
     */
    private Boolean isSystem = false;
} 