package com.hoxkar.permission.repository;

import com.hoxkar.permission.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联Repository
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    /**
     * 根据租户ID和用户ID查找用户角色关联
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.tenantId = :tenantId AND ur.userId = :userId")
    List<UserRole> findByTenantIdAndUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
    
    /**
     * 根据租户ID和角色ID查找用户角色关联
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.tenantId = :tenantId AND ur.roleId = :roleId")
    List<UserRole> findByTenantIdAndRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);
    
    /**
     * 根据租户ID、用户ID和角色ID查找用户角色关联
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.tenantId = :tenantId AND ur.userId = :userId AND ur.roleId = :roleId")
    UserRole findByTenantIdAndUserIdAndRoleId(@Param("tenantId") Long tenantId, @Param("userId") Long userId, @Param("roleId") Long roleId);
    
    /**
     * 删除用户的所有角色
     */
    @Query("DELETE FROM UserRole ur WHERE ur.tenantId = :tenantId AND ur.userId = :userId")
    void deleteByTenantIdAndUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);
    
    /**
     * 删除角色的所有用户
     */
    @Query("DELETE FROM UserRole ur WHERE ur.tenantId = :tenantId AND ur.roleId = :roleId")
    void deleteByTenantIdAndRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);
} 