package com.hoxkar.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限验证注解
 * 用于标记需要特定权限才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    
    /**
     * 权限代码
     */
    String value();
    
    /**
     * 权限代码数组（支持多个权限，满足其中一个即可）
     */
    String[] permissions() default {};
    
    /**
     * 逻辑类型：AND（需要所有权限）或OR（需要任一权限）
     */
    LogicType logic() default LogicType.OR;
    
    /**
     * 错误消息
     */
    String message() default "权限不足";
    
    /**
     * 逻辑类型枚举
     */
    enum LogicType {
        /**
         * 需要所有权限
         */
        AND,
        
        /**
         * 需要任一权限
         */
        OR
    }
} 