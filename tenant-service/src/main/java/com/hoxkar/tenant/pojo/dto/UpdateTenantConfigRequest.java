package com.hoxkar.tenant.pojo.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Update Tenant Config Request DTO
/**
 * 更新租户配置请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenantConfigRequest {
    
    /**
     * Config value // 配置值
     */
    private String configValue;
    
    /**
     * Config type // 配置类型
     */
    @Pattern(regexp = "^(STRING|NUMBER|BOOLEAN|JSON)$", message = "Config type must be STRING, NUMBER, BOOLEAN, or JSON") // 配置类型只能是STRING、NUMBER、BOOLEAN或JSON
    private String configType;
    
    /**
     * Config description // 配置描述
     */
    @Size(max = 200, message = "Config description length cannot exceed 200 characters") // 配置描述长度不能超过200个字符
    private String description;
}