package com.hoxkar.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

// Request ID Filter
/**
 * Request ID filter // 请求ID过滤器
 */
@Slf4j
@Component
public class RequestIdFilter extends AbstractGatewayFilterFactory<RequestIdFilter.Config> {
    public RequestIdFilter() {
        super(Config.class);
    }
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            // Get or generate request ID // 获取或生成请求ID
            String requestId = request.getHeaders().getFirst("X-Request-ID");
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }
            // Add request ID to headers // 添加请求ID到请求头
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();
            log.debug("Request ID generated: {}", requestId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
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