package com.hoxkar.common.exception;

import com.hoxkar.common.pojo.vo.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

// Global Exception Handler
/**
 * Global exception handler // 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handle business exception // 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage()); // 业务异常
        return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
    }
    /**
     * Handle validation exception // 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Validation exception: {}", message); // 参数校验异常
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(message));
    }
    /**
     * Handle bind exception // 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("Bind exception: {}", message); // 绑定异常
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(message));
    }
    /**
     * Handle constraint violation exception // 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("Constraint violation exception: {}", message); // 约束违反异常
        return ResponseEntity.badRequest().body(ApiResponse.badRequest(message));
    }
    /**
     * Handle authentication exception // 处理认证异常
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication exception: {}", e.getMessage()); // 认证异常
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.unauthorized());
    }
    /**
     * Handle bad credentials exception // 处理凭证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("Bad credentials exception: {}", e.getMessage()); // 凭证异常
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "Username or password is incorrect")); // 用户名或密码错误
    }
    /**
     * Handle access denied exception // 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied exception: {}", e.getMessage()); // 访问拒绝异常
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.forbidden());
    }
    /**
     * Handle pagination parameter exception // 处理分页参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaginationException(IllegalArgumentException e) {
        String message = e.getMessage();
        if (message != null && (message.contains("page") || message.contains("size") || message.contains("sort"))) {
            log.warn("Pagination parameter exception: {}", message); // 分页参数异常
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("Pagination parameter error: " + message)); // 分页参数错误
        }
        // If not pagination related, rethrow // 如果不是分页相关异常，继续抛出
        throw e;
    }
    /**
     * Handle JWT exception // 处理JWT异常
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtException(JwtException e) {
        log.warn("JWT exception: {}", e.getMessage()); // JWT异常
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, "Token is invalid or expired")); // Token无效或已过期
    }
    /**
     * Handle generic exception // 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("System exception: ", e); // 系统异常
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("Internal server error")); // 系统内部错误
    }
} 