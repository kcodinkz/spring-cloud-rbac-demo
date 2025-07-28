package com.hoxkar.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 租户验证注解
 * 用于标记需要租户上下文的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresTenant {
    
    /**
     * 是否必需租户上下文
     */
    boolean required() default true;
    
    /**
     * 错误消息
     */
    String message() default "租户上下文缺失";
} 