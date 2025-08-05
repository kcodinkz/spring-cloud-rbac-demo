// package com.hoxkar.common.util;

// import com.hoxkar.common.annotation.RequiresPermission;
// import com.hoxkar.common.annotation.RequiresRole;
// import com.hoxkar.common.exception.BusinessException;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Component;

// import jakarta.servlet.http.HttpServletRequest;

// // Permission Checker Utility
// /**
//  * Permission checker utility // 权限检查工具类
//  * Provides concrete implementations for permission and role validation // 提供权限和角色验证的具体实现
//  */
// @Slf4j
// @Component
// public class PermissionChecker {
//     /**
//      * Check permission // 检查权限
//      * @param request HTTP request // HTTP请求
//      * @param requiresPermission Permission annotation // 权限注解
//      */
//     public static void checkPermission(HttpServletRequest request, RequiresPermission requiresPermission) {
//         // TODO: Implement concrete permission checking logic // 实现具体的权限检查逻辑
//         // Should get user info from request, then check if user has the specified permission // 这里应该从请求中获取用户信息，然后检查用户是否具有指定权限
//         // Can use Feign to call permission-service for validation // 可以通过Feign调用permission-service进行权限验证
//         String permission = requiresPermission.value();
//         log.debug("Checking permission: {}", permission); // 检查权限
//         // Temporary implementation: log permission check, real project should call permission service // 临时实现：记录权限检查，实际项目中需要调用权限服务
//         if (!hasPermission(request, permission)) {
//             throw new BusinessException(requiresPermission.message());
//         }
//     }
//     /**
//      * Check role // 检查角色
//      * @param request HTTP request // HTTP请求
//      * @param requiresRole Role annotation // 角色注解
//      */
//     public static void checkRole(HttpServletRequest request, RequiresRole requiresRole) {
//         // TODO: Implement concrete role checking logic // 实现具体的角色检查逻辑
//         // Should get user info from request, then check if user has the specified role // 这里应该从请求中获取用户信息，然后检查用户是否具有指定角色
//         String role = requiresRole.value();
//         log.debug("Checking role: {}", role); // 检查角色
//         // Temporary implementation: log role check, real project should call permission service // 临时实现：记录角色检查，实际项目中需要调用权限服务
//         if (!hasRole(request, role)) {
//             throw new BusinessException(requiresRole.message());
//         }
//     }
//     /**
//      * Check if user has the specified permission // 检查用户是否具有指定权限
//      * @param request HTTP request // HTTP请求
//      * @param permission Permission code // 权限代码
//      * @return Whether has permission // 是否具有权限
//      */
//     private static boolean hasPermission(HttpServletRequest request, String permission) {
//         // TODO: Implement permission checking logic // 实现权限检查逻辑
//         // 1. Get user info from request header // 从请求头获取用户信息
//         // 2. Call permission-service to check permission // 调用permission-service检查权限
//         // 3. Return check result // 返回检查结果
//         log.debug("Checking user permission: {}", permission); // 检查用户权限
//         return true; // Temporary return true, should implement in real // 临时返回true，实际需要实现
//     }
//     /**
//      * Check if user has the specified role // 检查用户是否具有指定角色
//      * @param request HTTP request // HTTP请求
//      * @param role Role code // 角色代码
//      * @return Whether has role // 是否具有角色
//      */
//     private static boolean hasRole(HttpServletRequest request, String role) {
//         // TODO: Implement role checking logic // 实现角色检查逻辑
//         // 1. Get user info from request header // 从请求头获取用户信息
//         // 2. Call permission-service to check role // 调用permission-service检查角色
//         // 3. Return check result // 返回检查结果
//         log.debug("Checking user role: {}", role); // 检查用户角色
//         return true; // Temporary return true, should implement in real // 临时返回true，实际需要实现
//     }
// }
