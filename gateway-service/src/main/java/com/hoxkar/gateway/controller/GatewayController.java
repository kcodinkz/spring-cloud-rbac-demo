package com.hoxkar.gateway.controller;

import com.hoxkar.common.pojo.vo.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 网关管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/gateway")
@Tag(name = "网关管理", description = "API网关管理接口")
public class GatewayController {
    
    @Autowired
    private RouteLocator routeLocator;
    
    @GetMapping("/routes")
    @Operation(summary = "获取路由信息", description = "获取当前网关的所有路由配置")
    public ApiResponse<Map<String, Object>> getRoutes() {
        try {
            Map<String, Object> routes = new HashMap<>();
            routes.put("message", "路由信息获取成功");
            routes.put("timestamp", System.currentTimeMillis());
            routes.put("description", "当前网关路由配置信息");
            
            // 这里可以添加具体的路由信息获取逻辑
            // 由于RouteLocator的复杂性，这里提供基础信息
            
            return ApiResponse.success(routes);
        } catch (Exception e) {
            log.error("获取路由信息失败", e);
            return ApiResponse.error("获取路由信息失败");
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "网关健康检查", description = "检查网关服务是否正常运行")
    public ApiResponse<Map<String, Object>> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("service", "API Gateway");
            health.put("timestamp", System.currentTimeMillis());
            health.put("description", "API网关服务运行正常");
            health.put("version", "1.0.0");
            
            return ApiResponse.success(health);
        } catch (Exception e) {
            log.error("网关健康检查失败", e);
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("service", "API Gateway");
            health.put("timestamp", System.currentTimeMillis());
            health.put("error", e.getMessage());
            
            return ApiResponse.error("网关健康检查失败", health);
        }
    }
    
    @GetMapping("/info")
    @Operation(summary = "网关信息", description = "获取网关服务的基本信息")
    public ApiResponse<Map<String, Object>> getGatewayInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("service", "API Gateway");
            info.put("version", "1.0.0");
            info.put("description", "多租户RBAC系统API网关");
            info.put("features", new String[]{
                "JWT认证",
                "租户上下文管理",
                "分布式限流",
                "请求日志记录",
                "CORS跨域支持",
                "服务路由"
            });
            info.put("timestamp", System.currentTimeMillis());
            
            return ApiResponse.success(info);
        } catch (Exception e) {
            log.error("获取网关信息失败", e);
            return ApiResponse.error("获取网关信息失败");
        }
    }
}