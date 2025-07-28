package com.hoxkar.common.config;

import com.hoxkar.common.util.TenantContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign请求拦截器
 * 用于在服务间调用时传递租户上下文和用户信息
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {
        try {
            // 传递租户ID
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null) {
                template.header("X-Tenant-ID", tenantId);
                log.debug("Feign request - Tenant ID: {}", tenantId);
            }
            
            // 从当前请求中获取用户信息并传递
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 传递用户ID
                String userId = request.getHeader("X-User-ID");
                if (userId != null) {
                    template.header("X-User-ID", userId);
                }
                
                // 传递用户名
                String username = request.getHeader("X-Username");
                if (username != null) {
                    template.header("X-Username", username);
                }
                
                // 传递请求ID
                String requestId = request.getHeader("X-Request-ID");
                if (requestId != null) {
                    template.header("X-Request-ID", requestId);
                }
                
                log.debug("Feign request - User ID: {}, Username: {}, Request ID: {}", 
                        userId, username, requestId);
            }
            
        } catch (Exception e) {
            log.warn("Failed to apply Feign request interceptor", e);
        }
    }
} 