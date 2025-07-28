package com.hoxkar.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * 基于Redis的分布式限流过滤器
 */
@Slf4j
@Component
public class RateLimitFilter extends AbstractGatewayFilterFactory<RateLimitFilter.Config> {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis Lua脚本，实现原子性的限流逻辑
    private static final String RATE_LIMIT_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local limit = tonumber(ARGV[1])\n" +
        "local window = tonumber(ARGV[2])\n" +
        "local current = redis.call('GET', key)\n" +
        "if current == false then\n" +
        "    redis.call('SETEX', key, window, 1)\n" +
        "    return 1\n" +
        "else\n" +
        "    local count = tonumber(current)\n" +
        "    if count < limit then\n" +
        "        redis.call('INCR', key)\n" +
        "        return 1\n" +
        "    else\n" +
        "        return 0\n" +
        "    end\n" +
        "end";
    
    private final DefaultRedisScript<Long> rateLimitScript;
    
    public RateLimitFilter() {
        super(Config.class);
        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptText(RATE_LIMIT_SCRIPT);
        this.rateLimitScript.setResultType(Long.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientIp = getClientIp(exchange.getRequest());
            String path = exchange.getRequest().getPath().value();
            String key = "rate_limit:" + clientIp + ":" + path;
            
            try {
                // 执行Redis Lua脚本进行限流检查
                Long result = redisTemplate.execute(
                    rateLimitScript,
                    Arrays.asList(key),
                    config.getLimit(),
                    config.getWindow()
                );
                
                if (result != null && result == 1) {
                    // 限流通过，继续处理请求
                    log.debug("Rate limit passed for client: {}, path: {}", clientIp, path);
                    return chain.filter(exchange);
                } else {
                    // 限流超限，返回429状态码
                    log.warn("Rate limit exceeded for client: {}, path: {}", clientIp, path);
                    return handleRateLimitExceeded(exchange.getResponse(), config);
                }
                
            } catch (Exception e) {
                log.error("Rate limit check failed for client: {}, path: {}", clientIp, path, e);
                // 限流检查失败时，允许请求通过（降级处理）
                return chain.filter(exchange);
            }
        };
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(org.springframework.http.server.reactive.ServerHttpRequest request) {
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
     * 处理限流超限响应
     */
    private Mono<Void> handleRateLimitExceeded(ServerHttpResponse response, Config config) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String errorResponse = String.format(
            "{\"code\":429,\"message\":\"Rate limit exceeded. Please try again later.\",\"data\":null,\"timestamp\":%d,\"limit\":%d,\"window\":%d}",
            System.currentTimeMillis(), config.getLimit(), config.getWindow()
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 获取当前限流计数（用于监控）
     */
    public Long getCurrentCount(String clientIp, String path) {
        try {
            String key = "rate_limit:" + clientIp + ":" + path;
            Object value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.valueOf(value.toString()) : 0L;
        } catch (Exception e) {
            log.error("Failed to get current rate limit count", e);
            return 0L;
        }
    }
    
    /**
     * 重置限流计数（用于管理）
     */
    public void resetCount(String clientIp, String path) {
        try {
            String key = "rate_limit:" + clientIp + ":" + path;
            redisTemplate.delete(key);
            log.info("Rate limit count reset for client: {}, path: {}", clientIp, path);
        } catch (Exception e) {
            log.error("Failed to reset rate limit count", e);
        }
    }
    
    /**
     * 获取限流统计信息
     */
    public RateLimitStats getRateLimitStats(String clientIp, String path) {
        try {
            String key = "rate_limit:" + clientIp + ":" + path;
            Object value = redisTemplate.opsForValue().get(key);
            Long ttl = redisTemplate.getExpire(key);
            
            return RateLimitStats.builder()
                .currentCount(value != null ? Long.valueOf(value.toString()) : 0L)
                .remainingTtl(ttl != null ? ttl : 0L)
                .build();
        } catch (Exception e) {
            log.error("Failed to get rate limit stats", e);
            return RateLimitStats.builder()
                .currentCount(0L)
                .remainingTtl(0L)
                .build();
        }
    }
    
    /**
     * 过滤器配置类
     */
    public static class Config {
        private int limit = 100; // 默认限流阈值
        private int window = 60; // 默认时间窗口（秒）
        
        public int getLimit() {
            return limit;
        }
        
        public void setLimit(int limit) {
            this.limit = limit;
        }
        
        public int getWindow() {
            return window;
        }
        
        public void setWindow(int window) {
            this.window = window;
        }
    }
    
    /**
     * 限流统计信息
     */
    public static class RateLimitStats {
        private Long currentCount;
        private Long remainingTtl;
        
        public RateLimitStats() {}
        
        public RateLimitStats(Long currentCount, Long remainingTtl) {
            this.currentCount = currentCount;
            this.remainingTtl = remainingTtl;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public Long getCurrentCount() {
            return currentCount;
        }
        
        public void setCurrentCount(Long currentCount) {
            this.currentCount = currentCount;
        }
        
        public Long getRemainingTtl() {
            return remainingTtl;
        }
        
        public void setRemainingTtl(Long remainingTtl) {
            this.remainingTtl = remainingTtl;
        }
        
        public static class Builder {
            private Long currentCount;
            private Long remainingTtl;
            
            public Builder currentCount(Long currentCount) {
                this.currentCount = currentCount;
                return this;
            }
            
            public Builder remainingTtl(Long remainingTtl) {
                this.remainingTtl = remainingTtl;
                return this;
            }
            
            public RateLimitStats build() {
                return new RateLimitStats(currentCount, remainingTtl);
            }
        }
    }
} 