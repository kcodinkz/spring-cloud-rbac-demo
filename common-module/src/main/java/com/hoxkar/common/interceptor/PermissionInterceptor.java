package com.hoxkar.common.interceptor;

import com.hoxkar.common.annotation.RequiresPermission;
import com.hoxkar.common.annotation.RequiresRole;
import com.hoxkar.common.annotation.RequiresTenant;
import com.hoxkar.common.exception.BusinessException;
import com.hoxkar.common.util.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 权限验证拦截器
 * 用于在请求处理前进行权限验证
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        try {
            // 检查租户注解
            RequiresTenant requiresTenant = method.getAnnotation(RequiresTenant.class);
            if (requiresTenant != null) {
                checkTenant(requiresTenant);
            }
            
            // 检查权限注解
            RequiresPermission requiresPermission = method.getAnnotation(RequiresPermission.class);
            if (requiresPermission != null) {
                checkPermission(request, requiresPermission);
            }
            
            // 检查角色注解
            RequiresRole requiresRole = method.getAnnotation(RequiresRole.class);
            if (requiresRole != null) {
                checkRole(request, requiresRole);
            }
            
            return true;
            
        } catch (BusinessException e) {
            log.warn("权限验证失败: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                "{\"code\":403,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
                e.getMessage(), System.currentTimeMillis()
            ));
            return false;
        } catch (Exception e) {
            log.error("权限验证异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(String.format(
                "{\"code\":500,\"message\":\"权限验证异常\",\"data\":null,\"timestamp\":%d}",
                System.currentTimeMillis()
            ));
            return false;
        }
    }
    
    /**
     * 检查租户上下文
     */
    private void checkTenant(RequiresTenant requiresTenant) {
        String tenantId = TenantContext.getTenantId();
        
        if (requiresTenant.required() && (tenantId == null || tenantId.trim().isEmpty())) {
            throw new BusinessException(requiresTenant.message());
        }
        
        log.debug("租户验证通过 - 租户: {}", tenantId);
    }
    
    /**
     * 检查权限
     */
    private void checkPermission(HttpServletRequest request, RequiresPermission requiresPermission) {
        com.hoxkar.common.util.PermissionChecker.checkPermission(request, requiresPermission);
    }
    
    /**
     * 检查角色
     */
    private void checkRole(HttpServletRequest request, RequiresRole requiresRole) {
        com.hoxkar.common.util.PermissionChecker.checkRole(request, requiresRole);
    }
} 