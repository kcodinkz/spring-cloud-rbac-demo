package com.hoxkar.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

// Gateway Configuration
/**
 * Gateway configuration // 网关配置类
 */
@Configuration
public class GatewayConfig {
    /**
     * Configure CORS // 配置CORS跨域
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(Arrays.asList("*"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }
    /**
     * Configure routes (optional, can also be configured in application.yml) // 配置路由（可选，也可以在application.yml中配置）
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // Auth service route // 认证服务路由
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                )
                .uri("lb://auth-service"))
            // User service route // 用户服务路由
            .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                )
                .uri("lb://user-service"))
            // Permission service route // 权限服务路由
            .route("permission-service", r -> r
                .path("/api/permissions/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                )
                .uri("lb://permission-service"))
            // Tenant service route // 租户服务路由
            .route("tenant-service", r -> r
                .path("/api/tenants/**")
                .filters(f -> f
                    .stripPrefix(0)
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                )
                .uri("lb://tenant-service"))
            .build();
    }
    /**
     * Redis rate limiter configuration // Redis限流器配置
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(10, 20);
    }
    /**
     * Rate limit key resolver // 限流键解析器
     */
    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver userKeyResolver() {
        return exchange -> {
            String userKey = exchange.getRequest().getHeaders().getFirst("X-User-ID");
            if (userKey != null) {
                return reactor.core.publisher.Mono.just(userKey);
            }
            String clientIp = exchange.getRequest().getRemoteAddress() != null ? 
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
            return reactor.core.publisher.Mono.just(clientIp);
        };
    }
} 