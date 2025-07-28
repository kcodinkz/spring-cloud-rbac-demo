package com.hoxkar.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * 用户实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "real_name")
    private String realName;
    
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
    
    @Column(name = "is_super_admin", nullable = false)
    private Boolean isSuperAdmin = false;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // UserDetails接口实现
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 这里可以根据用户角色返回权限，暂时返回空集合
        return Collections.singletonList(new SimpleGrantedAuthority("USER"));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return "ACTIVE".equals(status);
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return "ACTIVE".equals(status);
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return "ACTIVE".equals(status);
    }
    
    @Override
    public boolean isEnabled() {
        return "ACTIVE".equals(status);
    }
} 