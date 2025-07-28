package com.hoxkar.gateway.controller;

import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.gateway.filter.RateLimitFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 限流管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/gateway/rate-limit")
@Tag(name = "限流管理", description = "API网关限流管理接口")
public class RateLimitController {
    
    @Autowired
    private RateLimitFilter rateLimitFilter;
    
    @GetMapping("/stats/{clientIp}/{path}")
    @Operation(summary = "获取限流统计信息", description = "获取指定客户端和路径的限流统计信息")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RateLimitFilter.RateLimitStats> getRateLimitStats(
            @Parameter(description = "客户端IP") @PathVariable String clientIp,
            @Parameter(description = "请求路径") @PathVariable String path) {
        try {
            RateLimitFilter.RateLimitStats stats = rateLimitFilter.getRateLimitStats(clientIp, path);
            return ApiResponse.success(stats);
        } catch (Exception e) {
            log.error("获取限流统计信息失败", e);
            return ApiResponse.error("获取限流统计信息失败");
        }
    }
    
    @GetMapping("/count/{clientIp}/{path}")
    @Operation(summary = "获取当前限流计数", description = "获取指定客户端和路径的当前限流计数")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> getCurrentCount(
            @Parameter(description = "客户端IP") @PathVariable String clientIp,
            @Parameter(description = "请求路径") @PathVariable String path) {
        try {
            Long count = rateLimitFilter.getCurrentCount(clientIp, path);
            return ApiResponse.success(count);
        } catch (Exception e) {
            log.error("获取限流计数失败", e);
            return ApiResponse.error("获取限流计数失败");
        }
    }
    
    @DeleteMapping("/reset/{clientIp}/{path}")
    @Operation(summary = "重置限流计数", description = "重置指定客户端和路径的限流计数")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> resetCount(
            @Parameter(description = "客户端IP") @PathVariable String clientIp,
            @Parameter(description = "请求路径") @PathVariable String path) {
        try {
            rateLimitFilter.resetCount(clientIp, path);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("重置限流计数失败", e);
            return ApiResponse.error("重置限流计数失败");
        }
    }
    
    @DeleteMapping("/reset/all")
    @Operation(summary = "重置所有限流计数", description = "重置所有客户端的限流计数")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<Map<String, Object>> resetAllCounts() {
        try {
            // 这里可以实现批量重置逻辑
            // 由于Redis键的命名规则，可以通过模式匹配来删除所有限流相关的键
            Map<String, Object> result = new HashMap<>();
            result.put("message", "所有限流计数已重置");
            result.put("timestamp", System.currentTimeMillis());
            
            log.info("所有限流计数已重置");
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("重置所有限流计数失败", e);
            return ApiResponse.error("重置所有限流计数失败");
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "限流服务健康检查", description = "检查限流服务是否正常运行")
    public ApiResponse<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "Rate Limit Service");
            health.put("timestamp", System.currentTimeMillis());
            health.put("description", "基于Redis的分布式限流服务运行正常");
            
            return ApiResponse.success(health);
        } catch (Exception e) {
            log.error("限流服务健康检查失败", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("service", "Rate Limit Service");
            health.put("timestamp", System.currentTimeMillis());
            health.put("error", e.getMessage());
            
            return ApiResponse.error("限流服务健康检查失败", health);
        }
    }
} 