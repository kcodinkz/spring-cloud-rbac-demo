package com.hoxkar.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 多租户上下文管理
 */
@Slf4j
public class TenantContext {
    
    private static final ThreadLocal<String> TENANT_ID = new ThreadLocal<>();
    
    /**
     * 设置租户ID
     */
    public static void setTenantId(String tenantId) {
        log.debug("设置租户ID: {}", tenantId);
        TENANT_ID.set(tenantId);
    }
    
    /**
     * 获取租户ID
     */
    public static String getTenantId() {
        String tenantId = TENANT_ID.get();
        log.debug("获取租户ID: {}", tenantId);
        return tenantId;
    }
    
    /**
     * 清除租户ID
     */
    public static void clear() {
        log.debug("清除租户ID");
        TENANT_ID.remove();
    }
    
    /**
     * 检查是否有租户ID
     */
    public static boolean hasTenantId() {
        return TENANT_ID.get() != null;
    }
    
    /**
     * 获取租户ID（如果为空则抛出异常）
     */
    public static String getRequiredTenantId() {
        String tenantId = getTenantId();
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("租户ID不能为空");
        }
        return tenantId;
    }
} 