package com.hoxkar.tenant.controller;

import com.hoxkar.tenant.pojo.dto.*;
import com.hoxkar.tenant.pojo.vo.*;
import com.hoxkar.tenant.service.TenantService;
import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.common.pojo.dto.PageRequestDTO;
import com.hoxkar.common.pojo.vo.PageResponseVO;
import com.hoxkar.common.util.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 租户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Tag(name = "租户管理", description = "租户管理相关接口")
public class TenantController {
    
    private final TenantService tenantService;
    
    // 租户管理接口
    @PostMapping
    @Operation(summary = "创建租户", description = "创建新的租户")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantVO> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return tenantService.createTenant(request);
    }
    
    @PutMapping("/{tenantId}")
    @Operation(summary = "更新租户", description = "更新租户信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantVO> updateTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId,
            @Valid @RequestBody UpdateTenantRequest request) {
        return tenantService.updateTenant(tenantId, request);
    }
    
    @DeleteMapping("/{tenantId}")
    @Operation(summary = "删除租户", description = "删除租户")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> deleteTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId) {
        return tenantService.deleteTenant(tenantId);
    }
    
    @GetMapping("/{tenantId}")
    @Operation(summary = "根据ID获取租户", description = "根据租户ID获取租户信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantVO> getTenantById(
            @Parameter(description = "租户ID") @PathVariable Long tenantId) {
        return tenantService.getTenantById(tenantId);
    }
    
    @GetMapping("/code/{tenantCode}")
    @Operation(summary = "根据租户代码获取租户", description = "根据租户代码获取租户信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantVO> getTenantByCode(
            @Parameter(description = "租户代码") @PathVariable String tenantCode) {
        return tenantService.getTenantByCode(tenantCode);
    }
    
    @GetMapping("/domain/{domain}")
    @Operation(summary = "根据域名获取租户", description = "根据域名获取租户信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantVO> getTenantByDomain(
            @Parameter(description = "域名") @PathVariable String domain) {
        return tenantService.getTenantByDomain(domain);
    }
    
    @GetMapping
    @Operation(summary = "获取所有租户", description = "获取所有租户列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantVO>> getAllTenants() {
        return tenantService.getAllTenants();
    }
    
    @PostMapping("/page")
    @Operation(summary = "分页获取租户", description = "分页获取租户列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<PageResponseVO<TenantVO>> getTenantsByPage(@Valid @RequestBody PageRequestDTO pageRequestDTO) {
        Pageable pageable = PageUtils.toPageable(pageRequestDTO);
        Page<TenantVO> page = tenantService.getTenantsByPage(pageable).getData();
        PageResponseVO<TenantVO> vo = PageUtils.toPageResponseVO(page);
        return ApiResponse.success(vo);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取租户", description = "根据状态获取租户列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantVO>> getTenantsByStatus(
            @Parameter(description = "状态") @PathVariable String status) {
        return tenantService.getTenantsByStatus(status);
    }
    
    @GetMapping("/plan/{subscriptionPlan}")
    @Operation(summary = "根据订阅计划获取租户", description = "根据订阅计划获取租户列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantVO>> getTenantsBySubscriptionPlan(
            @Parameter(description = "订阅计划") @PathVariable String subscriptionPlan) {
        return tenantService.getTenantsBySubscriptionPlan(subscriptionPlan);
    }
    
    @GetMapping("/search")
    @Operation(summary = "搜索租户", description = "根据租户名称搜索租户")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantVO>> searchTenants(
            @Parameter(description = "租户名称") @RequestParam String tenantName) {
        return tenantService.searchTenants(tenantName);
    }
    
    @PostMapping("/search/advanced")
    @Operation(summary = "高级搜索租户", description = "根据复杂条件分页搜索租户")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<PageResponseVO<TenantVO>> searchTenantsAdvanced(@Valid @RequestBody TenantSearchRequest request) {
        Pageable pageable = PageUtils.toPageable(request.getPageRequest());
        Page<TenantVO> page = tenantService.searchTenantsAdvanced(request, pageable).getData();
        PageResponseVO<TenantVO> vo = PageUtils.toPageResponseVO(page);
        return ApiResponse.success(vo);
    }
    
    @PutMapping("/{tenantId}/enable")
    @Operation(summary = "启用租户", description = "启用租户")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> enableTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId) {
        return tenantService.enableTenant(tenantId);
    }
    
    @PutMapping("/{tenantId}/disable")
    @Operation(summary = "禁用租户", description = "禁用租户")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> disableTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId) {
        return tenantService.disableTenant(tenantId);
    }
    
    @PutMapping("/{tenantId}/user-count")
    @Operation(summary = "更新租户用户数", description = "更新租户当前用户数")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> updateTenantUserCount(
            @Parameter(description = "租户ID") @PathVariable String tenantId,
            @Parameter(description = "用户数") @RequestParam Integer userCount) {
        return tenantService.updateTenantUserCount(tenantId, userCount);
    }
    
    @GetMapping("/expiring")
    @Operation(summary = "获取即将过期的租户", description = "获取即将过期的租户列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantVO>> getExpiringTenants(
            @Parameter(description = "天数") @RequestParam(defaultValue = "30") Integer days) {
        return tenantService.getExpiringTenants(days);
    }
    
    @GetMapping("/expired")
    @Operation(summary = "获取已过期的租户", description = "获取已过期的租户列表")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantVO>> getExpiredTenants() {
        return tenantService.getExpiredTenants();
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "获取租户统计信息", description = "获取租户统计信息")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> getTenantStatistics() {
        return tenantService.getTenantStatistics();
    }
    
    // 租户配置管理接口
    @PostMapping("/configs")
    @Operation(summary = "创建租户配置", description = "创建租户配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantConfigVO> createTenantConfig(@Valid @RequestBody CreateTenantConfigRequest request) {
        return tenantService.createTenantConfig(request);
    }
    
    @PutMapping("/configs/{configId}")
    @Operation(summary = "更新租户配置", description = "更新租户配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantConfigVO> updateTenantConfig(
            @Parameter(description = "配置ID") @PathVariable Long configId,
            @Valid @RequestBody UpdateTenantConfigRequest request) {
        return tenantService.updateTenantConfig(configId, request);
    }
    
    @DeleteMapping("/configs/{configId}")
    @Operation(summary = "删除租户配置", description = "删除租户配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> deleteTenantConfig(
            @Parameter(description = "配置ID") @PathVariable Long configId) {
        return tenantService.deleteTenantConfig(configId);
    }
    
    @GetMapping("/configs/{configId}")
    @Operation(summary = "根据ID获取租户配置", description = "根据配置ID获取租户配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantConfigVO> getTenantConfigById(
            @Parameter(description = "配置ID") @PathVariable Long configId) {
        return tenantService.getTenantConfigById(configId);
    }
    
    @GetMapping("/configs")
    @Operation(summary = "获取租户配置", description = "根据租户ID和配置键获取配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantConfigVO> getTenantConfig(
            @Parameter(description = "租户ID") @RequestParam String tenantId,
            @Parameter(description = "配置键") @RequestParam String configKey) {
        return tenantService.getTenantConfig(tenantId, configKey);
    }
    
    @GetMapping("/configs/all")
    @Operation(summary = "获取租户所有配置", description = "获取租户的所有配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantConfigVO>> getTenantConfigs(
            @Parameter(description = "租户ID") @RequestParam String tenantId) {
        return tenantService.getTenantConfigs(tenantId);
    }
    
    @GetMapping("/configs/type")
    @Operation(summary = "根据配置类型获取租户配置", description = "根据配置类型获取租户配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantConfigVO>> getTenantConfigsByType(
            @Parameter(description = "租户ID") @RequestParam String tenantId,
            @Parameter(description = "配置类型") @RequestParam String configType) {
        return tenantService.getTenantConfigsByType(tenantId, configType);
    }
    
    @PostMapping("/configs/batch")
    @Operation(summary = "批量设置租户配置", description = "批量设置租户配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> batchSetTenantConfigs(
            @Parameter(description = "租户ID") @RequestParam String tenantId,
            @RequestBody Map<String, String> configs) {
        return tenantService.batchSetTenantConfigs(tenantId, configs);
    }
    
    @DeleteMapping("/configs/all")
    @Operation(summary = "删除租户所有配置", description = "删除租户的所有配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Void> deleteTenantConfigs(
            @Parameter(description = "租户ID") @RequestParam String tenantId) {
        return tenantService.deleteTenantConfigs(tenantId);
    }
    
    @GetMapping("/configs/system")
    @Operation(summary = "获取系统配置", description = "获取租户的系统配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantConfigVO>> getSystemConfigs(
            @Parameter(description = "租户ID") @RequestParam String tenantId) {
        return tenantService.getSystemConfigs(tenantId);
    }
    
    @GetMapping("/configs/user")
    @Operation(summary = "获取用户配置", description = "获取租户的用户配置")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<List<TenantConfigVO>> getUserConfigs(
            @Parameter(description = "租户ID") @RequestParam String tenantId) {
        return tenantService.getUserConfigs(tenantId);
    }
} 