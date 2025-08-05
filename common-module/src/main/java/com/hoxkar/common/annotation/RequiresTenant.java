package com.hoxkar.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Requires Tenant Annotation
/**
 * Tenant validation annotation // 租户验证注解
 * Used to mark methods that require tenant context // 用于标记需要租户上下文的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresTenant {
    /**
     * Whether tenant context is required // 是否必需租户上下文
     */
    boolean required() default true;
    /**
     * Error message // 错误消息
     */
    String message() default "Tenant context missing"; // 租户上下文缺失
} 