package com.hoxkar.tenant.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户配置视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantConfigVO {
    
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 配置键
     */
    private String configKey;
    
    /**
     * 配置值
     */
    private String configValue;
    
    /**
     * 配置类型
     */
    private String configType;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 是否为系统配置
     */
    private Boolean isSystem;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 