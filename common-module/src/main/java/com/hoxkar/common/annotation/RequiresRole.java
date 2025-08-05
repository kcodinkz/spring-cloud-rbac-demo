package com.hoxkar.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Requires Role Annotation
/**
 * Role validation annotation // 角色验证注解
 * Used to mark methods that require specific roles to access // 用于标记需要特定角色才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    /**
     * Role code // 角色代码
     */
    String value() default "";
    /**
     * Role code array (supports multiple roles, any one is sufficient) // 角色代码数组（支持多个角色，满足其中一个即可）
     */
    String[] roles() default {};
    /**
     * Logic type: AND (all roles required) or OR (any role required) // 逻辑类型：AND（需要所有角色）或OR（需要任一角色）
     */
    LogicType logic() default LogicType.OR;
    /**
     * Error message // 错误消息
     */
    String message() default "Insufficient role permission"; // 角色权限不足
    /**
     * Logic type enum // 逻辑类型枚举
     */
    enum LogicType {
        /**
         * All roles required // 需要所有角色
         */
        AND,
        /**
         * Any role required // 需要任一角色
         */
        OR
    }
} 