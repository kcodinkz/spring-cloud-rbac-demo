package com.hoxkar.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色验证注解
 * 用于标记需要特定角色才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    
    /**
     * 角色代码
     */
    String value() default "";
    
    /**
     * 角色代码数组（支持多个角色，满足其中一个即可）
     */
    String[] roles() default {};
    
    /**
     * 逻辑类型：AND（需要所有角色）或OR（需要任一角色）
     */
    LogicType logic() default LogicType.OR;
    
    /**
     * 错误消息
     */
    String message() default "角色权限不足";
    
    /**
     * 逻辑类型枚举
     */
    enum LogicType {
        /**
         * 需要所有角色
         */
        AND,
        
        /**
         * 需要任一角色
         */
        OR
    }
} 