package com.hoxkar.tenant.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 创建租户配置请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantConfigRequest {
    
    /**
     * 租户ID
     */
    @NotBlank(message = "租户ID不能为空")
    private String tenantId;
    
    /**
     * 配置键
     */
    @NotBlank(message = "配置键不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "配置键只能包含字母、数字、点、下划线和连字符")
    @Size(max = 100, message = "配置键长度不能超过100个字符")
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型
     */
    @Pattern(regexp = "^(STRING|NUMBER|BOOLEAN|JSON)$", message = "配置类型只能是STRING、NUMBER、BOOLEAN或JSON")
    private String configType = "STRING";
    
    /**
     * 配置描述
     */
    @Size(max = 200, message = "配置描述长度不能超过200个字符")
    private String description;
    
    /**
     * 是否为系统配置
     */
    private Boolean isSystem = false;
} 