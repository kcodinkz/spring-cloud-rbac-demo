package com.hoxkar.common.exception;

/**
 * JWT异常
 */
public class JwtException extends RuntimeException {
    
    /**
     * 构造函数
     */
    public JwtException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     */
    public JwtException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 创建JWT异常
     */
    public static JwtException of(String message) {
        return new JwtException(message);
    }
    
    /**
     * 创建JWT异常
     */
    public static JwtException of(String message, Throwable cause) {
        return new JwtException(message, cause);
    }
} 