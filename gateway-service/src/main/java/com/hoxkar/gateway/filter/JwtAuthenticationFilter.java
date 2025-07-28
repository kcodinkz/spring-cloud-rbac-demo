package com.hoxkar.gateway.filter;

import com.hoxkar.common.util.JwtTokenUtil;
import com.hoxkar.common.util.TenantContext;
import com.hoxkar.common.exception.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {
    
    private final JwtTokenUtil jwtTokenUtil;
    
    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
        super(Config.class);
        this.jwtTokenUtil = jwtTokenUtil;
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            
            String path = request.getPath().value();
            
            // 跳过不需要认证的路径
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }
            
            // 获取Authorization头
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                return handleUnauthorized(response, "Missing or invalid Authorization header");
            }
            
            String token = authHeader.substring(7);
            
            try {
                // 验证JWT令牌
                if (!jwtTokenUtil.validateToken(token)) {
                    return handleUnauthorized(response, "Invalid JWT token");
                }
                
                // 检查令牌是否在黑名单中
                if (jwtTokenUtil.isTokenBlacklisted(token)) {
                    return handleUnauthorized(response, "Token is blacklisted");
                }
                
                // 提取租户ID并设置到上下文
                String tenantId = jwtTokenUtil.getTenantIdFromToken(token);
                if (StringUtils.hasText(tenantId)) {
                    TenantContext.setTenantId(tenantId);
                }
                
                // 提取用户信息并添加到请求头
                String userId = jwtTokenUtil.getUserIdFromToken(token);
                String username = jwtTokenUtil.getUsernameFromToken(token);
                
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-ID", userId)
                    .header("X-Username", username)
                    .header("X-Tenant-ID", tenantId)
                    .build();
                
                log.debug("JWT authentication successful for user: {}, tenant: {}", username, tenantId);
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build())
                    .doFinally(signalType -> TenantContext.clear());
                
            } catch (JwtException e) {
                log.warn("JWT authentication failed: {}", e.getMessage());
                return handleUnauthorized(response, e.getMessage());
            } catch (Exception e) {
                log.error("JWT authentication error", e);
                return handleUnauthorized(response, "Authentication failed");
            }
        };
    }
    
    /**
     * 判断是否为公开路径
     */
    private boolean isPublicPath(String path) {
        List<String> publicPaths = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/health",
            "/api/gateway/health",
            "/api/gateway/info",
            "/actuator/health",
            "/actuator/info",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-ui.html"
        );
        
        return publicPaths.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 处理未授权响应
     */
    private Mono<Void> handleUnauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String errorResponse = String.format(
            "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
            message, System.currentTimeMillis()
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 过滤器配置类
     */
    public static class Config {
        // 可以添加配置属性
    }
} 