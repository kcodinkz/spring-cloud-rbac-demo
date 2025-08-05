 package com.hoxkar.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Logging Filter
/**
 * Logging filter // 请求日志记录过滤器
 */
@Slf4j
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {
    public LoggingFilter() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();
            String method = request.getMethod().name();
            String clientIp = getClientIp(request);
            String userAgent = request.getHeaders().getFirst("User-Agent");
            String userId = request.getHeaders().getFirst("X-User-ID");
            String tenantId = request.getHeaders().getFirst("X-Tenant-ID");
            long startTime = System.currentTimeMillis();
            log.info("Gateway Request - Method: {}, Path: {}, ClientIP: {}, UserID: {}, TenantID: {}, UserAgent: {}, Time: {}",
                    method, path, clientIp, userId, tenantId, userAgent, 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            return chain.filter(exchange)
                .doFinally(signalType -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    log.info("Gateway Response - Method: {}, Path: {}, Duration: {}ms, Status: {}, Time: {}",
                            method, path, duration, exchange.getResponse().getStatusCode(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                });
        };
    }
    /**
     * Get client IP // 获取客户端IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    /**
     * Filter config class // 过滤器配置类
     */
    public static class Config {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
}