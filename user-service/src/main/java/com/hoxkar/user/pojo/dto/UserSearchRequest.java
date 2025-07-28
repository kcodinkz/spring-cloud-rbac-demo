package com.hoxkar.user.pojo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 用户搜索请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchRequest {
    
    /**
     * 用户名（模糊查询）
     */
    private String username;
    
    /**
     * 真实姓名（模糊查询）
     */
    private String realName;
    
    /**
     * 邮箱（模糊查询）
     */
    private String email;
    
    /**
     * 手机号（模糊查询）
     */
    private String phone;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 是否为超级管理员
     */
    private Boolean isSuperAdmin;
    
    /**
     * 页码
     */
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
} 