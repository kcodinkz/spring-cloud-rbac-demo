package com.hoxkar.tenant.repository;

import com.hoxkar.tenant.entity.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 租户配置Repository
 */
@Repository
public interface TenantConfigRepository extends JpaRepository<TenantConfig, Long> {
    
    /**
     * 根据租户ID和配置键查找配置
     */
    Optional<TenantConfig> findByTenantIdAndConfigKey(Long tenantId, String configKey);
    
    /**
     * 根据租户ID查找所有配置
     */
    @Query("SELECT tc FROM TenantConfig tc WHERE tc.tenantId = :tenantId")
    List<TenantConfig> findByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 根据租户ID和配置类型查找配置
     */
    @Query("SELECT tc FROM TenantConfig tc WHERE tc.tenantId = :tenantId AND tc.configType = :configType")
    List<TenantConfig> findByTenantIdAndConfigType(@Param("tenantId") Long tenantId, @Param("configType") String configType);
    
    /**
     * 根据租户ID和系统配置标志查找配置
     */
    @Query("SELECT tc FROM TenantConfig tc WHERE tc.tenantId = :tenantId AND tc.isSystem = :isSystem")
    List<TenantConfig> findByTenantIdAndIsSystem(@Param("tenantId") Long tenantId, @Param("isSystem") Boolean isSystem);
    
    /**
     * 根据配置键查找所有租户的配置
     */
    @Query("SELECT tc FROM TenantConfig tc WHERE tc.configKey = :configKey")
    List<TenantConfig> findByConfigKey(@Param("configKey") String configKey);
    
    /**
     * 根据配置类型查找所有租户的配置
     */
    @Query("SELECT tc FROM TenantConfig tc WHERE tc.configType = :configType")
    List<TenantConfig> findByConfigType(@Param("configType") String configType);
    
    /**
     * 检查租户ID和配置键是否存在
     */
    boolean existsByTenantIdAndConfigKey(Long tenantId, String configKey);
    
    /**
     * 删除租户的所有配置
     */
    @Query("DELETE FROM TenantConfig tc WHERE tc.tenantId = :tenantId")
    void deleteByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 删除租户的指定配置
     */
    @Query("DELETE FROM TenantConfig tc WHERE tc.tenantId = :tenantId AND tc.configKey = :configKey")
    void deleteByTenantIdAndConfigKey(@Param("tenantId") Long tenantId, @Param("configKey") String configKey);
} 