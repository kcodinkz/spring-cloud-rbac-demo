package com.hoxkar.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Requires Permission Annotation
/**
 * Permission validation annotation // 权限验证注解
 * Used to mark methods that require specific permissions to access // 用于标记需要特定权限才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    /**
     * Permission code // 权限代码
     */
    String value();
    /**
     * Permission code array (supports multiple permissions, any one is sufficient) // 权限代码数组（支持多个权限，满足其中一个即可）
     */
    String[] permissions() default {};
    /**
     * Logic type: AND (all permissions required) or OR (any permission required) // 逻辑类型：AND（需要所有权限）或OR（需要任一权限）
     */
    LogicType logic() default LogicType.OR;
    /**
     * Error message // 错误消息
     */
    String message() default "Insufficient permission"; // 权限不足
    /**
     * Logic type enum // 逻辑类型枚举
     */
    enum LogicType {
        /**
         * All permissions required // 需要所有权限
         */
        AND,
        /**
         * Any permission required // 需要任一权限
         */
        OR
    }
} 