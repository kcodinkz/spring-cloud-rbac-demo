package com.hoxkar.common.aspect;

import com.hoxkar.common.annotation.RequiresPermission;
import com.hoxkar.common.annotation.RequiresRole;
import com.hoxkar.common.annotation.RequiresTenant;
import com.hoxkar.common.exception.BusinessException;
import com.hoxkar.common.util.TenantContext;
import com.hoxkar.common.client.PermissionServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

// Permission Validation Aspect
/**
 * Permission validation aspect // 权限验证切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {
    private final PermissionServiceClient permissionServiceClient;
    /**
     * Permission validation aspect // 权限验证切面
     */
    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        try {
            String[] permissions = getPermissions(requiresPermission);
            if (permissions.length == 0) {
                return;
            }
            Long userId = getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("User not logged in"); // 用户未登录
            }
            boolean hasPermission = checkUserPermissions(userId, permissions, requiresPermission.logic());
            if (!hasPermission) {
                throw new BusinessException(requiresPermission.message());
            }
            log.debug("Permission validation passed - User: {}, Permissions: {}", userId, Arrays.toString(permissions)); // 权限验证通过
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Permission validation failed", e); // 权限验证失败
            throw new BusinessException("Permission validation failed"); // 权限验证失败
        }
    }
    /**
     * Role validation aspect // 角色验证切面
     */
    @Before("@annotation(requiresRole)")
    public void checkRole(JoinPoint joinPoint, RequiresRole requiresRole) {
        try {
            String[] roles = getRoles(requiresRole);
            if (roles.length == 0) {
                return;
            }
            Long userId = getCurrentUserId();
            if (userId == null) {
                throw new BusinessException("User not logged in"); // 用户未登录
            }
            boolean hasRole = checkUserRoles(userId, roles, requiresRole.logic());
            if (!hasRole) {
                throw new BusinessException(requiresRole.message());
            }
            log.debug("Role validation passed - User: {}, Roles: {}", userId, Arrays.toString(roles)); // 角色验证通过
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Role validation failed", e); // 角色验证失败
            throw new BusinessException("Role validation failed"); // 角色验证失败
        }
    }
    /**
     * Tenant validation aspect // 租户验证切面
     */
    @Before("@annotation(requiresTenant)")
    public void checkTenant(JoinPoint joinPoint, RequiresTenant requiresTenant) {
        try {
            String tenantId = TenantContext.getTenantId();
            if (requiresTenant.required() && (tenantId == null || tenantId.trim().isEmpty())) {
                throw new BusinessException(requiresTenant.message());
            }
            log.debug("Tenant validation passed - Tenant: {}", tenantId); // 租户验证通过
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Tenant validation failed", e); // 租户验证失败
            throw new BusinessException("Tenant validation failed"); // 租户验证失败
        }
    }
    /**
     * Get permission array // 获取权限数组
     */
    private String[] getPermissions(RequiresPermission requiresPermission) {
        if (requiresPermission.permissions().length > 0) {
            return requiresPermission.permissions();
        }
        return new String[]{requiresPermission.value()};
    }
    /**
     * Get role array // 获取角色数组
     */
    private String[] getRoles(RequiresRole requiresRole) {
        if (requiresRole.roles().length > 0) {
            return requiresRole.roles();
        }
        return new String[]{requiresRole.value()};
    }
    /**
     * Get current user ID // 获取当前用户ID
     */
    private Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userIdStr = request.getHeader("X-User-ID");
            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                try {
                    return Long.valueOf(userIdStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid user ID: {}", userIdStr); // 无效的用户ID
                }
            }
        }
        return null;
    }
    /**
     * Check user permissions // 检查用户权限
     */
    private boolean checkUserPermissions(Long userId, String[] permissions, RequiresPermission.LogicType logic) {
        try {
            if (logic == RequiresPermission.LogicType.AND) {
                // All permissions required // 需要所有权限
                for (String permission : permissions) {
                    var response = permissionServiceClient.checkUserPermission(userId, permission);
                    if (!Boolean.TRUE.equals(response.getData())) {
                        return false;
                    }
                }
                return true;
            } else {
                // Any permission required // 需要任一权限
                for (String permission : permissions) {
                    var response = permissionServiceClient.checkUserPermission(userId, permission);
                    if (Boolean.TRUE.equals(response.getData())) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            log.error("Check user permission failed", e); // 检查用户权限失败
            return false;
        }
    }
    /**
     * Check user roles // 检查用户角色
     */
    private boolean checkUserRoles(Long userId, String[] roles, RequiresRole.LogicType logic) {
        try {
            if (logic == RequiresRole.LogicType.AND) {
                // All roles required // 需要所有角色
                for (String role : roles) {
                    var response = permissionServiceClient.checkUserRole(userId, role);
                    if (!Boolean.TRUE.equals(response.getData())) {
                        return false;
                    }
                }
                return true;
            } else {
                // Any role required // 需要任一角色
                for (String role : roles) {
                    var response = permissionServiceClient.checkUserRole(userId, role);
                    if (Boolean.TRUE.equals(response.getData())) {
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            log.error("Check user role failed", e); // 检查用户角色失败
            return false;
        }
    }
} 