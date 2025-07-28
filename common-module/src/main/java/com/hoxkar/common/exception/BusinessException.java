package com.hoxkar.common.exception;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 构造函数
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    /**
     * 构造函数
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造函数
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }
    
    /**
     * 构造函数
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    /**
     * 创建业务异常
     */
    public static BusinessException of(String message) {
        return new BusinessException(message);
    }
    
    /**
     * 创建业务异常
     */
    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }
    
    /**
     * 创建业务异常
     */
    public static BusinessException of(String message, Throwable cause) {
        return new BusinessException(message, cause);
    }
    
    /**
     * 创建业务异常
     */
    public static BusinessException of(Integer code, String message, Throwable cause) {
        return new BusinessException(code, message, cause);
    }
} 