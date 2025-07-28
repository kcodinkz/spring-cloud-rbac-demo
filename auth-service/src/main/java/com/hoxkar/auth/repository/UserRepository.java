package com.hoxkar.auth.repository;

import com.hoxkar.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据租户ID和用户名查找用户
     */
    Optional<User> findByTenantIdAndUsername(Long tenantId, String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 根据租户ID和邮箱查找用户
     */
    Optional<User> findByTenantIdAndEmail(Long tenantId, String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查租户ID和用户名是否存在
     */
    boolean existsByTenantIdAndUsername(Long tenantId, String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 检查租户ID和邮箱是否存在
     */
    boolean existsByTenantIdAndEmail(Long tenantId, String email);
    
    /**
     * 根据租户ID查找所有用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId")
    java.util.List<User> findAllByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 根据租户ID和状态查找用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = :status")
    java.util.List<User> findByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status);
} 