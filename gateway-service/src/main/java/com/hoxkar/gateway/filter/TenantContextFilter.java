package com.hoxkar.gateway.filter;

import com.hoxkar.common.util.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

/**
 * 租户上下文过滤器
 */
@Slf4j
@Component
public class TenantContextFilter extends AbstractGatewayFilterFactory<TenantContextFilter.Config> {
    
    public TenantContextFilter() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // 从请求头获取租户ID
            String tenantId = request.getHeaders().getFirst("X-Tenant-ID");
            
            // 如果没有从JWT中获取到租户ID，尝试从请求头获取
            if (!StringUtils.hasText(tenantId)) {
                tenantId = request.getHeaders().getFirst("tenant-id");
            }
            
            // 如果还是没有，尝试从查询参数获取
            if (!StringUtils.hasText(tenantId)) {
                tenantId = request.getQueryParams().getFirst("tenantId");
            }
            
            // 设置租户上下文
            if (StringUtils.hasText(tenantId)) {
                TenantContext.setTenantId(tenantId);
                log.debug("Tenant context set: {}", tenantId);
            } else {
                log.debug("No tenant ID found in request");
            }
            
            return chain.filter(exchange)
                .doFinally(signalType -> {
                    TenantContext.clear();
                    log.debug("Tenant context cleared");
                });
        };
    }
    
    /**
     * 过滤器配置类
     */
    public static class Config {
        // 可以添加配置属性
    }
} 