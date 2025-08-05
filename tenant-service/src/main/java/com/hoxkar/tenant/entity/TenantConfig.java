package com.hoxkar.tenant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

// Tenant Config Entity
/**
 * Tenant config entity // 租户配置实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenant_configs")
public class TenantConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "config_key", nullable = false)
    private String configKey;
    
    @Column(name = "config_value")
    private String configValue;
    
    @Column(name = "config_type")
    private String configType; // STRING, NUMBER, BOOLEAN, JSON
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 