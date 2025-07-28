package com.hoxkar.tenant.service.impl;

import com.hoxkar.tenant.entity.Tenant;
import com.hoxkar.tenant.entity.TenantConfig;
import com.hoxkar.tenant.pojo.dto.*;
import com.hoxkar.tenant.pojo.vo.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import com.hoxkar.tenant.repository.TenantRepository;
import com.hoxkar.tenant.repository.TenantConfigRepository;
import com.hoxkar.tenant.service.TenantService;
import com.hoxkar.common.exception.BusinessException;
import com.hoxkar.common.pojo.vo.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 租户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {
    
    private final TenantRepository tenantRepository;
    private final TenantConfigRepository tenantConfigRepository;
    
    // 租户管理
    @Override
    @Transactional
    public ApiResponse<TenantVO> createTenant(CreateTenantRequest request) {
        try {
            // 检查租户代码是否已存在
            if (tenantRepository.existsByTenantCode(request.getTenantCode())) {
                throw BusinessException.of("租户代码已存在");
            }
            
            // 检查域名是否已存在
            if (request.getDomain() != null && tenantRepository.existsByDomain(request.getDomain())) {
                throw BusinessException.of("域名已存在");
            }
            
            // 检查联系邮箱是否已存在
            if (request.getContactEmail() != null && tenantRepository.existsByContactEmail(request.getContactEmail())) {
                throw BusinessException.of("联系邮箱已存在");
            }
            
            // 创建租户实体
            Tenant tenant = new Tenant();
            tenant.setTenantCode(request.getTenantCode());
            tenant.setTenantName(request.getTenantName());
            tenant.setDescription(request.getDescription());
            tenant.setContactPerson(request.getContactPerson());
            tenant.setContactEmail(request.getContactEmail());
            tenant.setContactPhone(request.getContactPhone());
            tenant.setAddress(request.getAddress());
            tenant.setDomain(request.getDomain());
            tenant.setLogoUrl(request.getLogoUrl());
            tenant.setStatus("ACTIVE");
            tenant.setMaxUsers(request.getMaxUsers());
            tenant.setCurrentUsers(0);
            tenant.setSubscriptionPlan(request.getSubscriptionPlan());
            tenant.setSubscriptionStartDate(request.getSubscriptionStartDate());
            tenant.setSubscriptionEndDate(request.getSubscriptionEndDate());
            
            // 保存租户
            Tenant savedTenant = tenantRepository.save(tenant);
            
            log.info("创建租户成功: {}", savedTenant.getTenantName());
            return ApiResponse.success("租户创建成功", convertToTenantVO(savedTenant));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建租户失败: ", e);
            throw BusinessException.of("创建租户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<TenantVO> updateTenant(Long tenantId, UpdateTenantRequest request) {
        try {
            // 查找租户
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> BusinessException.of("租户不存在"));
            
            // 检查域名是否已被其他租户使用
            if (request.getDomain() != null && !request.getDomain().equals(tenant.getDomain())) {
                if (tenantRepository.existsByDomain(request.getDomain())) {
                    throw BusinessException.of("域名已被其他租户使用");
                }
                tenant.setDomain(request.getDomain());
            }
            
            // 检查联系邮箱是否已被其他租户使用
            if (request.getContactEmail() != null && !request.getContactEmail().equals(tenant.getContactEmail())) {
                if (tenantRepository.existsByContactEmail(request.getContactEmail())) {
                    throw BusinessException.of("联系邮箱已被其他租户使用");
                }
                tenant.setContactEmail(request.getContactEmail());
            }
            
            // 更新租户信息
            if (request.getTenantName() != null) {
                tenant.setTenantName(request.getTenantName());
            }
            if (request.getDescription() != null) {
                tenant.setDescription(request.getDescription());
            }
            if (request.getContactPerson() != null) {
                tenant.setContactPerson(request.getContactPerson());
            }
            if (request.getContactPhone() != null) {
                tenant.setContactPhone(request.getContactPhone());
            }
            if (request.getAddress() != null) {
                tenant.setAddress(request.getAddress());
            }
            if (request.getLogoUrl() != null) {
                tenant.setLogoUrl(request.getLogoUrl());
            }
            if (request.getStatus() != null) {
                tenant.setStatus(request.getStatus());
            }
            if (request.getMaxUsers() != null) {
                tenant.setMaxUsers(request.getMaxUsers());
            }
            if (request.getSubscriptionPlan() != null) {
                tenant.setSubscriptionPlan(request.getSubscriptionPlan());
            }
            if (request.getSubscriptionStartDate() != null) {
                tenant.setSubscriptionStartDate(request.getSubscriptionStartDate());
            }
            if (request.getSubscriptionEndDate() != null) {
                tenant.setSubscriptionEndDate(request.getSubscriptionEndDate());
            }
            
            // 保存租户
            Tenant updatedTenant = tenantRepository.save(tenant);
            
            log.info("更新租户成功: {}", updatedTenant.getTenantName());
            return ApiResponse.success("租户更新成功", convertToTenantVO(updatedTenant));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新租户失败: ", e);
            throw BusinessException.of("更新租户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteTenant(Long tenantId) {
        try {
            // 查找租户
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> BusinessException.of("租户不存在"));
            
            // 检查租户是否有用户
            if (tenant.getCurrentUsers() > 0) {
                throw BusinessException.of("租户还有用户，不能删除");
            }
            
            // 删除租户
            tenantRepository.delete(tenant);
            
            // 删除租户的所有配置
            tenantConfigRepository.deleteByTenantId(tenant.getId());
            
            log.info("租户 {} 删除成功", tenant.getTenantName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除租户失败: ", e);
            throw BusinessException.of("删除租户失败");
        }
    }
    
    @Override
    public ApiResponse<TenantVO> getTenantById(Long tenantId) {
        try {
            // 查找租户
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> BusinessException.of("租户不存在"));
            
            return ApiResponse.success(convertToTenantVO(tenant));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取租户失败: ", e);
            throw BusinessException.of("获取租户失败");
        }
    }
    
    @Override
    public ApiResponse<TenantVO> getTenantByCode(String tenantCode) {
        try {
            // 查找租户
            Tenant tenant = tenantRepository.findByTenantCode(tenantCode)
                    .orElseThrow(() -> BusinessException.of("租户不存在"));
            
            return ApiResponse.success(convertToTenantVO(tenant));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取租户失败: ", e);
            throw BusinessException.of("获取租户失败");
        }
    }
    
    @Override
    public ApiResponse<TenantVO> getTenantByDomain(String domain) {
        try {
            // 查找租户
            Tenant tenant = tenantRepository.findByDomain(domain)
                    .orElseThrow(() -> BusinessException.of("租户不存在"));
            
            return ApiResponse.success(convertToTenantVO(tenant));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取租户失败: ", e);
            throw BusinessException.of("获取租户失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantVO>> getAllTenants() {
        try {
            // 查找所有租户
            List<Tenant> tenants = tenantRepository.findAll();
            List<TenantVO> tenantVOs = tenants.stream()
                    .map(this::convertToTenantVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(tenantVOs);
            
        } catch (Exception e) {
            log.error("获取租户列表失败: ", e);
            throw BusinessException.of("获取租户列表失败");
        }
    }
    
    @Override
    public ApiResponse<Page<TenantVO>> getTenantsByPage(Pageable pageable) {
        try {
            // 分页查找租户
            Page<Tenant> tenantPage = tenantRepository.findAll(pageable);
            Page<TenantVO> tenantVOPage = tenantPage.map(this::convertToTenantVO);
            
            return ApiResponse.success(tenantVOPage);
            
        } catch (Exception e) {
            log.error("分页获取租户失败: ", e);
            throw BusinessException.of("分页获取租户失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantVO>> getTenantsByStatus(String status) {
        try {
            // 根据状态查找租户
            List<Tenant> tenants = tenantRepository.findByStatus(status);
            List<TenantVO> tenantVOs = tenants.stream()
                    .map(this::convertToTenantVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(tenantVOs);
            
        } catch (Exception e) {
            log.error("根据状态获取租户失败: ", e);
            throw BusinessException.of("根据状态获取租户失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantVO>> getTenantsBySubscriptionPlan(String subscriptionPlan) {
        try {
            // 根据订阅计划查找租户
            List<Tenant> tenants = tenantRepository.findBySubscriptionPlan(subscriptionPlan);
            List<TenantVO> tenantVOs = tenants.stream()
                    .map(this::convertToTenantVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(tenantVOs);
            
        } catch (Exception e) {
            log.error("根据订阅计划获取租户失败: ", e);
            throw BusinessException.of("根据订阅计划获取租户失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantVO>> searchTenants(String tenantName) {
        try {
            // 根据租户名称搜索租户
            List<Tenant> tenants = tenantRepository.findByTenantNameContaining(tenantName);
            List<TenantVO> tenantVOs = tenants.stream()
                    .map(this::convertToTenantVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(tenantVOs);
            
        } catch (Exception e) {
            log.error("搜索租户失败: ", e);
            throw BusinessException.of("搜索租户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> enableTenant(Long tenantId) {
        try {
            // 查找租户
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> BusinessException.of("租户不存在"));
            
            // 启用租户
            tenant.setStatus("ACTIVE");
            tenantRepository.save(tenant);
            
            log.info("租户 {} 启用成功", tenant.getTenantName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("启用租户失败: ", e);
            throw BusinessException.of("启用租户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> disableTenant(Long tenantId) {
        try {
            // 查找租户
            Tenant tenant = tenantRepository.findById(tenantId)
                    .orElseThrow(() -> BusinessException.of("租户不存在"));
            
            // 禁用租户
            tenant.setStatus("INACTIVE");
            tenantRepository.save(tenant);
            
            log.info("租户 {} 禁用成功", tenant.getTenantName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("禁用租户失败: ", e);
            throw BusinessException.of("禁用租户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> updateTenantUserCount(String tenantId, Integer userCount) {
        try {
            // 查找租户
            Tenant tenant = tenantRepository.findByTenantCode(tenantId)
                    .orElseThrow(() -> BusinessException.of("租户不存在"));
            
            // 检查用户数是否超过最大限制
            if (tenant.getMaxUsers() != null && userCount > tenant.getMaxUsers()) {
                throw BusinessException.of("用户数超过最大限制");
            }
            
            // 更新用户数
            tenant.setCurrentUsers(userCount);
            tenantRepository.save(tenant);
            
            log.info("租户 {} 用户数更新成功", tenant.getTenantName());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新租户用户数失败: ", e);
            throw BusinessException.of("更新租户用户数失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantVO>> getExpiringTenants(Integer days) {
        try {
            // 计算过期时间
            LocalDateTime endDate = LocalDateTime.now().plusDays(days);
            
            // 查找即将过期的租户
            List<Tenant> tenants = tenantRepository.findExpiringTenants(endDate);
            List<TenantVO> tenantVOs = tenants.stream()
                    .map(this::convertToTenantVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(tenantVOs);
            
        } catch (Exception e) {
            log.error("获取即将过期的租户失败: ", e);
            throw BusinessException.of("获取即将过期的租户失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantVO>> getExpiredTenants() {
        try {
            // 查找已过期的租户
            List<Tenant> tenants = tenantRepository.findExpiredTenants(LocalDateTime.now());
            List<TenantVO> tenantVOs = tenants.stream()
                    .map(this::convertToTenantVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(tenantVOs);
            
        } catch (Exception e) {
            log.error("获取已过期的租户失败: ", e);
            throw BusinessException.of("获取已过期的租户失败");
        }
    }
    
    @Override
    public ApiResponse<Map<String, Object>> getTenantStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 统计各状态的租户数量
            List<Object[]> statusCounts = tenantRepository.countByStatus();
            Map<String, Long> statusStats = statusCounts.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]
                    ));
            statistics.put("statusStats", statusStats);
            
            // 统计各订阅计划的租户数量
            List<Object[]> planCounts = tenantRepository.countBySubscriptionPlan();
            Map<String, Long> planStats = planCounts.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]
                    ));
            statistics.put("planStats", planStats);
            
            // 总租户数
            long totalTenants = tenantRepository.count();
            statistics.put("totalTenants", totalTenants);
            
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("获取租户统计信息失败: ", e);
            throw BusinessException.of("获取租户统计信息失败");
        }
    }
    
    // 租户配置管理
    @Override
    @Transactional
    public ApiResponse<TenantConfigVO> createTenantConfig(CreateTenantConfigRequest request) {
        try {
            // 检查配置是否已存在
            if (tenantConfigRepository.existsByTenantIdAndConfigKey(Long.valueOf(request.getTenantId()), request.getConfigKey())) {
                throw BusinessException.of("配置已存在");
            }
            
            // 创建租户配置实体
            TenantConfig config = new TenantConfig();
            config.setTenantId(Long.valueOf(request.getTenantId()));
            config.setConfigKey(request.getConfigKey());
            config.setConfigValue(request.getConfigValue());
            config.setConfigType(request.getConfigType());
            config.setDescription(request.getDescription());
            config.setIsSystem(request.getIsSystem());
            
            // 保存配置
            TenantConfig savedConfig = tenantConfigRepository.save(config);
            
            log.info("创建租户配置成功: tenantId={}, configKey={}", request.getTenantId(), request.getConfigKey());
            return ApiResponse.success("配置创建成功", convertToTenantConfigVO(savedConfig));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建租户配置失败: ", e);
            throw BusinessException.of("创建租户配置失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<TenantConfigVO> updateTenantConfig(Long configId, UpdateTenantConfigRequest request) {
        try {
            // 查找配置
            TenantConfig config = tenantConfigRepository.findById(configId)
                    .orElseThrow(() -> BusinessException.of("配置不存在"));
            
            // 更新配置信息
            if (request.getConfigValue() != null) {
                config.setConfigValue(request.getConfigValue());
            }
            if (request.getConfigType() != null) {
                config.setConfigType(request.getConfigType());
            }
            if (request.getDescription() != null) {
                config.setDescription(request.getDescription());
            }
            
            // 保存配置
            TenantConfig updatedConfig = tenantConfigRepository.save(config);
            
            log.info("更新租户配置成功: configId={}", configId);
            return ApiResponse.success("配置更新成功", convertToTenantConfigVO(updatedConfig));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新租户配置失败: ", e);
            throw BusinessException.of("更新租户配置失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteTenantConfig(Long configId) {
        try {
            // 查找配置
            TenantConfig config = tenantConfigRepository.findById(configId)
                    .orElseThrow(() -> BusinessException.of("配置不存在"));
            
            // 检查是否为系统配置
            if (config.getIsSystem()) {
                throw BusinessException.of("系统配置不能删除");
            }
            
            // 删除配置
            tenantConfigRepository.delete(config);
            
            log.info("租户配置 {} 删除成功", config.getConfigKey());
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除租户配置失败: ", e);
            throw BusinessException.of("删除租户配置失败");
        }
    }
    
    @Override
    public ApiResponse<TenantConfigVO> getTenantConfigById(Long configId) {
        try {
            // 查找配置
            TenantConfig config = tenantConfigRepository.findById(configId)
                    .orElseThrow(() -> BusinessException.of("配置不存在"));
            
            return ApiResponse.success(convertToTenantConfigVO(config));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取租户配置失败: ", e);
            throw BusinessException.of("获取租户配置失败");
        }
    }
    
    @Override
    public ApiResponse<TenantConfigVO> getTenantConfig(String tenantId, String configKey) {
        try {
            // 查找配置
            TenantConfig config = tenantConfigRepository.findByTenantIdAndConfigKey(Long.valueOf(tenantId), configKey)
                    .orElseThrow(() -> BusinessException.of("配置不存在"));
            
            return ApiResponse.success(convertToTenantConfigVO(config));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取租户配置失败: ", e);
            throw BusinessException.of("获取租户配置失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantConfigVO>> getTenantConfigs(String tenantId) {
        try {
            // 查找租户的所有配置
            List<TenantConfig> configs = tenantConfigRepository.findByTenantId(Long.valueOf(tenantId));
            List<TenantConfigVO> configVOs = configs.stream()
                    .map(this::convertToTenantConfigVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(configVOs);
            
        } catch (Exception e) {
            log.error("获取租户配置失败: ", e);
            throw BusinessException.of("获取租户配置失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantConfigVO>> getTenantConfigsByType(String tenantId, String configType) {
        try {
            // 根据配置类型查找租户配置
            List<TenantConfig> configs = tenantConfigRepository.findByTenantIdAndConfigType(Long.valueOf(tenantId), configType);
            List<TenantConfigVO> configVOs = configs.stream()
                    .map(this::convertToTenantConfigVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(configVOs);
            
        } catch (Exception e) {
            log.error("根据配置类型获取租户配置失败: ", e);
            throw BusinessException.of("根据配置类型获取租户配置失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchSetTenantConfigs(String tenantId, Map<String, String> configs) {
        try {
            for (Map.Entry<String, String> entry : configs.entrySet()) {
                String configKey = entry.getKey();
                String configValue = entry.getValue();
                
                // 查找或创建配置
                TenantConfig config = tenantConfigRepository.findByTenantIdAndConfigKey(Long.valueOf(tenantId), configKey)
                        .orElse(new TenantConfig());
                
                config.setTenantId(Long.valueOf(tenantId));
                config.setConfigKey(configKey);
                config.setConfigValue(configValue);
                config.setConfigType("STRING");
                config.setIsSystem(false);
                
                tenantConfigRepository.save(config);
            }
            
            log.info("租户 {} 配置批量设置成功", tenantId);
            return ApiResponse.success(null);
            
        } catch (Exception e) {
            log.error("批量设置租户配置失败: ", e);
            throw BusinessException.of("批量设置租户配置失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteTenantConfigs(String tenantId) {
        try {
            // 删除租户的所有配置
            tenantConfigRepository.deleteByTenantId(Long.valueOf(tenantId));
            
            log.info("租户 {} 所有配置删除成功", tenantId);
            return ApiResponse.success(null);
            
        } catch (Exception e) {
            log.error("删除租户配置失败: ", e);
            throw BusinessException.of("删除租户配置失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantConfigVO>> getSystemConfigs(String tenantId) {
        try {
            // 查找系统配置
            List<TenantConfig> configs = tenantConfigRepository.findByTenantIdAndIsSystem(Long.valueOf(tenantId), true);
            List<TenantConfigVO> configVOs = configs.stream()
                    .map(this::convertToTenantConfigVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(configVOs);
            
        } catch (Exception e) {
            log.error("获取系统配置失败: ", e);
            throw BusinessException.of("获取系统配置失败");
        }
    }
    
    @Override
    public ApiResponse<List<TenantConfigVO>> getUserConfigs(String tenantId) {
        try {
            // 查找用户配置
            List<TenantConfig> configs = tenantConfigRepository.findByTenantIdAndIsSystem(Long.valueOf(tenantId), false);
            List<TenantConfigVO> configVOs = configs.stream()
                    .map(this::convertToTenantConfigVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(configVOs);
            
        } catch (Exception e) {
            log.error("获取用户配置失败: ", e);
            throw BusinessException.of("获取用户配置失败");
        }
    }
    
    /**
     * 将Tenant实体转换为TenantVO
     */
    private TenantVO convertToTenantVO(Tenant tenant) {
        TenantVO vo = new TenantVO();
        vo.setId(tenant.getId());
        vo.setTenantCode(tenant.getTenantCode());
        vo.setTenantName(tenant.getTenantName());
        vo.setDescription(tenant.getDescription());
        vo.setContactPerson(tenant.getContactPerson());
        vo.setContactEmail(tenant.getContactEmail());
        vo.setContactPhone(tenant.getContactPhone());
        vo.setAddress(tenant.getAddress());
        vo.setDomain(tenant.getDomain());
        vo.setLogoUrl(tenant.getLogoUrl());
        vo.setStatus(tenant.getStatus());
        vo.setMaxUsers(tenant.getMaxUsers());
        vo.setCurrentUsers(tenant.getCurrentUsers());
        vo.setSubscriptionPlan(tenant.getSubscriptionPlan());
        vo.setSubscriptionStartDate(tenant.getSubscriptionStartDate());
        vo.setSubscriptionEndDate(tenant.getSubscriptionEndDate());
        vo.setCreatedAt(tenant.getCreatedAt());
        vo.setUpdatedAt(tenant.getUpdatedAt());
        return vo;
    }
    
    /**
     * 将TenantConfig实体转换为TenantConfigVO
     */
    private TenantConfigVO convertToTenantConfigVO(TenantConfig config) {
        TenantConfigVO vo = new TenantConfigVO();
        vo.setId(config.getId());
        vo.setTenantId(config.getTenantId().toString());
        vo.setConfigKey(config.getConfigKey());
        vo.setConfigValue(config.getConfigValue());
        vo.setConfigType(config.getConfigType());
        vo.setDescription(config.getDescription());
        vo.setIsSystem(config.getIsSystem());
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
    
    // 新增方法实现
    @Override
    public ApiResponse<Page<TenantVO>> searchTenantsAdvanced(TenantSearchRequest request, Pageable pageable) {
        try {
            // 根据搜索条件查找租户
            Page<Tenant> tenantPage = null;
            
            if (StringUtils.hasText(request.getTenantName())) {
                tenantPage = tenantRepository.findByTenantNameContaining(request.getTenantName(), pageable);
            } else if (StringUtils.hasText(request.getTenantCode())) {
                tenantPage = tenantRepository.findByTenantCodeContaining(request.getTenantCode(), pageable);
            } else if (StringUtils.hasText(request.getDomain())) {
                tenantPage = tenantRepository.findByDomainContaining(request.getDomain(), pageable);
            } else if (StringUtils.hasText(request.getContactEmail())) {
                tenantPage = tenantRepository.findByContactEmailContaining(request.getContactEmail(), pageable);
            } else if (StringUtils.hasText(request.getStatus())) {
                tenantPage = tenantRepository.findByStatus(request.getStatus(), pageable);
            } else if (StringUtils.hasText(request.getSubscriptionPlan())) {
                tenantPage = tenantRepository.findBySubscriptionPlan(request.getSubscriptionPlan(), pageable);
            } else {
                // 默认查询所有租户
                tenantPage = tenantRepository.findAll(pageable);
            }
            
            if (tenantPage != null) {
                Page<TenantVO> tenantVOPage = tenantPage.map(this::convertToTenantVO);
                return ApiResponse.success(tenantVOPage);
            } else {
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("高级搜索租户失败: ", e);
            throw BusinessException.of("高级搜索租户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchEnableTenants(List<Long> tenantIds) {
        try {
            // 批量启用租户
            for (Long tenantId : tenantIds) {
                Tenant tenant = tenantRepository.findById(tenantId)
                        .orElseThrow(() -> BusinessException.of("租户不存在: " + tenantId));
                
                tenant.setStatus("ACTIVE");
                tenantRepository.save(tenant);
            }
            
            log.info("批量启用租户成功: {}", tenantIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量启用租户失败: ", e);
            throw BusinessException.of("批量启用租户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDisableTenants(List<Long> tenantIds) {
        try {
            // 批量禁用租户
            for (Long tenantId : tenantIds) {
                Tenant tenant = tenantRepository.findById(tenantId)
                        .orElseThrow(() -> BusinessException.of("租户不存在: " + tenantId));
                
                tenant.setStatus("INACTIVE");
                tenantRepository.save(tenant);
            }
            
            log.info("批量禁用租户成功: {}", tenantIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量禁用租户失败: ", e);
            throw BusinessException.of("批量禁用租户失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeleteTenants(List<Long> tenantIds) {
        try {
            // 批量删除租户
            for (Long tenantId : tenantIds) {
                Tenant tenant = tenantRepository.findById(tenantId)
                        .orElseThrow(() -> BusinessException.of("租户不存在: " + tenantId));
                
                // 检查租户是否有用户
                if (tenant.getCurrentUsers() > 0) {
                    throw BusinessException.of("租户还有用户，不能删除: " + tenantId);
                }
                
                tenantRepository.delete(tenant);
                
                // 删除租户的所有配置
                tenantConfigRepository.deleteByTenantId(tenant.getId());
            }
            
            log.info("批量删除租户成功: {}", tenantIds);
            return ApiResponse.success(null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除租户失败: ", e);
            throw BusinessException.of("批量删除租户失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkTenantCodeExists(String tenantCode) {
        try {
            boolean exists = tenantRepository.existsByTenantCode(tenantCode);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查租户代码是否存在失败: ", e);
            throw BusinessException.of("检查租户代码是否存在失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkDomainExists(String domain) {
        try {
            boolean exists = tenantRepository.existsByDomain(domain);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查域名是否存在失败: ", e);
            throw BusinessException.of("检查域名是否存在失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkContactEmailExists(String contactEmail) {
        try {
            boolean exists = tenantRepository.existsByContactEmail(contactEmail);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查联系邮箱是否存在失败: ", e);
            throw BusinessException.of("检查联系邮箱是否存在失败");
        }
    }
    
    @Override
    public ApiResponse<TenantStatisticsVO> getTenantStatisticsDetailed() {
        try {
            TenantStatisticsVO statistics = new TenantStatisticsVO();
            
            // 基础统计信息
            Long totalTenants = tenantRepository.count();
            Long activeTenants = tenantRepository.countByStatus("ACTIVE");
            Long inactiveTenants = tenantRepository.countByStatus("INACTIVE");
            
            // 过期统计信息
            Long expiringTenants = tenantRepository.countExpiringTenants(LocalDateTime.now().plusDays(30));
            Long expiredTenants = tenantRepository.countExpiredTenants(LocalDateTime.now());
            
            // 状态统计
            List<Object[]> statusCounts = tenantRepository.countByStatus();
            Map<String, Long> statusStats = statusCounts.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]
                    ));
            
            // 订阅计划统计
            List<Object[]> planCounts = tenantRepository.countBySubscriptionPlan();
            Map<String, Long> planStats = planCounts.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]
                    ));
            
            // 用户统计
            Long totalUsers = tenantRepository.sumCurrentUsers();
            Double averageUsersPerTenant = totalTenants > 0 ? (double) totalUsers / totalTenants : 0.0;
            
            // 本月统计
            Long newTenantsThisMonth = tenantRepository.countByCreatedAtAfter(LocalDateTime.now().withDayOfMonth(1));
            // 由于Tenant实体没有lastLoginTime字段，暂时使用活跃租户总数作为本月活跃租户数
            Long activeTenantsThisMonth = tenantRepository.countByStatus("ACTIVE");
            
            // 设置统计信息
            statistics.setTotalTenants(totalTenants);
            statistics.setActiveTenants(activeTenants);
            statistics.setInactiveTenants(inactiveTenants);
            statistics.setExpiringTenants(expiringTenants);
            statistics.setExpiredTenants(expiredTenants);
            statistics.setStatusStats(statusStats);
            statistics.setPlanStats(planStats);
            statistics.setTotalUsers(totalUsers);
            statistics.setAverageUsersPerTenant(averageUsersPerTenant);
            statistics.setNewTenantsThisMonth(newTenantsThisMonth);
            statistics.setActiveTenantsThisMonth(activeTenantsThisMonth);
            
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("获取租户详细统计信息失败: ", e);
            throw BusinessException.of("获取租户详细统计信息失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeleteTenantConfigs(String tenantId, List<String> configKeys) {
        try {
            for (String configKey : configKeys) {
                TenantConfig config = tenantConfigRepository.findByTenantIdAndConfigKey(Long.valueOf(tenantId), configKey)
                        .orElse(null);
                
                if (config != null && !config.getIsSystem()) {
                    tenantConfigRepository.delete(config);
                }
            }
            
            log.info("租户 {} 配置批量删除成功", tenantId);
            return ApiResponse.success(null);
            
        } catch (Exception e) {
            log.error("批量删除租户配置失败: ", e);
            throw BusinessException.of("批量删除租户配置失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkConfigKeyExists(String tenantId, String configKey) {
        try {
            boolean exists = tenantConfigRepository.existsByTenantIdAndConfigKey(Long.valueOf(tenantId), configKey);
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查配置键是否存在失败: ", e);
            throw BusinessException.of("检查配置键是否存在失败");
        }
    }
} 