package com.hoxkar.user.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Create User Request DTO
/**
 * 创建用户请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    
    /**
     * Tenant ID // 租户ID
     */
    @NotBlank(message = "Tenant ID cannot be empty") // 租户ID不能为空
    private String tenantId;
    
    /**
     * Username // 用户名
     */
    @NotBlank(message = "Username cannot be empty") // 用户名不能为空
    @Size(min = 3, max = 50, message = "Username length must be between 3 and 50 characters") // 用户名长度必须在3-50个字符之间
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores") // 用户名只能包含字母、数字和下划线
    private String username;
    
    /**
     * Password // 密码
     */
    @NotBlank(message = "Password cannot be empty") // 密码不能为空
    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100 characters") // 密码长度必须在6-100个字符之间
    private String password;
    
    /**
     * Email // 邮箱
     */
    @Email(message = "Invalid email format") // 邮箱格式不正确
    private String email;
    
    /**
     * Phone number // 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "Invalid phone number format") // 手机号格式不正确
    private String phone;
    
    /**
     * Real name // 真实姓名
     */
    @NotBlank(message = "Real name cannot be empty") // 真实姓名不能为空
    @Size(max = 50, message = "Real name length cannot exceed 50 characters") // 真实姓名长度不能超过50个字符
    private String realName;
    
    /**
     * Is super admin // 是否为超级管理员
     */
    private Boolean isSuperAdmin = false;
} 