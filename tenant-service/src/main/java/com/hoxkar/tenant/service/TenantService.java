package com.hoxkar.tenant.service;

import com.hoxkar.tenant.pojo.dto.*;
import com.hoxkar.tenant.pojo.vo.*;
import com.hoxkar.common.pojo.vo.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 租户服务接口
 */
public interface TenantService {
    
    // 租户管理
    /**
     * 创建租户
     */
    ApiResponse<TenantVO> createTenant(CreateTenantRequest request);
    
    /**
     * 更新租户
     */
    ApiResponse<TenantVO> updateTenant(Long tenantId, UpdateTenantRequest request);
    
    /**
     * 删除租户
     */
    ApiResponse<Void> deleteTenant(Long tenantId);
    
    /**
     * 根据ID获取租户
     */
    ApiResponse<TenantVO> getTenantById(Long tenantId);
    
    /**
     * 根据租户代码获取租户
     */
    ApiResponse<TenantVO> getTenantByCode(String tenantCode);
    
    /**
     * 根据域名获取租户
     */
    ApiResponse<TenantVO> getTenantByDomain(String domain);
    
    /**
     * 获取所有租户
     */
    ApiResponse<List<TenantVO>> getAllTenants();
    
    /**
     * 分页获取租户
     */
    ApiResponse<Page<TenantVO>> getTenantsByPage(Pageable pageable);
    
    /**
     * 根据状态获取租户
     */
    ApiResponse<List<TenantVO>> getTenantsByStatus(String status);
    
    /**
     * 根据订阅计划获取租户
     */
    ApiResponse<List<TenantVO>> getTenantsBySubscriptionPlan(String subscriptionPlan);
    
    /**
     * 搜索租户
     */
    ApiResponse<List<TenantVO>> searchTenants(String tenantName);
    
    /**
     * 启用租户
     */
    ApiResponse<Void> enableTenant(Long tenantId);
    
    /**
     * 禁用租户
     */
    ApiResponse<Void> disableTenant(Long tenantId);
    
    /**
     * 更新租户用户数
     */
    ApiResponse<Void> updateTenantUserCount(String tenantId, Integer userCount);
    
    /**
     * 获取即将过期的租户
     */
    ApiResponse<List<TenantVO>> getExpiringTenants(Integer days);
    
    /**
     * 获取已过期的租户
     */
    ApiResponse<List<TenantVO>> getExpiredTenants();
    
    /**
     * 获取租户统计信息
     */
    ApiResponse<Map<String, Object>> getTenantStatistics();
    
    /**
     * 高级搜索租户
     */
    ApiResponse<Page<TenantVO>> searchTenantsAdvanced(TenantSearchRequest request, Pageable pageable);
    
    /**
     * 批量启用租户
     */
    ApiResponse<Void> batchEnableTenants(List<Long> tenantIds);
    
    /**
     * 批量禁用租户
     */
    ApiResponse<Void> batchDisableTenants(List<Long> tenantIds);
    
    /**
     * 批量删除租户
     */
    ApiResponse<Void> batchDeleteTenants(List<Long> tenantIds);
    
    /**
     * 检查租户代码是否存在
     */
    ApiResponse<Boolean> checkTenantCodeExists(String tenantCode);
    
    /**
     * 检查域名是否存在
     */
    ApiResponse<Boolean> checkDomainExists(String domain);
    
    /**
     * 检查联系邮箱是否存在
     */
    ApiResponse<Boolean> checkContactEmailExists(String contactEmail);
    
    /**
     * 获取租户详细统计信息
     */
    ApiResponse<TenantStatisticsVO> getTenantStatisticsDetailed();
    
    // 租户配置管理
    /**
     * 创建租户配置
     */
    ApiResponse<TenantConfigVO> createTenantConfig(CreateTenantConfigRequest request);
    
    /**
     * 更新租户配置
     */
    ApiResponse<TenantConfigVO> updateTenantConfig(Long configId, UpdateTenantConfigRequest request);
    
    /**
     * 删除租户配置
     */
    ApiResponse<Void> deleteTenantConfig(Long configId);
    
    /**
     * 根据ID获取租户配置
     */
    ApiResponse<TenantConfigVO> getTenantConfigById(Long configId);
    
    /**
     * 根据租户ID和配置键获取配置
     */
    ApiResponse<TenantConfigVO> getTenantConfig(String tenantId, String configKey);
    
    /**
     * 获取租户的所有配置
     */
    ApiResponse<List<TenantConfigVO>> getTenantConfigs(String tenantId);
    
    /**
     * 根据配置类型获取租户配置
     */
    ApiResponse<List<TenantConfigVO>> getTenantConfigsByType(String tenantId, String configType);
    
    /**
     * 批量设置租户配置
     */
    ApiResponse<Void> batchSetTenantConfigs(String tenantId, Map<String, String> configs);
    
    /**
     * 删除租户的所有配置
     */
    ApiResponse<Void> deleteTenantConfigs(String tenantId);
    
    /**
     * 获取系统配置
     */
    ApiResponse<List<TenantConfigVO>> getSystemConfigs(String tenantId);
    
    /**
     * 获取用户配置
     */
    ApiResponse<List<TenantConfigVO>> getUserConfigs(String tenantId);
    
    /**
     * 批量删除租户配置
     */
    ApiResponse<Void> batchDeleteTenantConfigs(String tenantId, List<String> configKeys);
    
    /**
     * 检查配置键是否存在
     */
    ApiResponse<Boolean> checkConfigKeyExists(String tenantId, String configKey);
} 