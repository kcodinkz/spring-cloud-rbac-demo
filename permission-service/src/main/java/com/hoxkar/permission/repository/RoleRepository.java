package com.hoxkar.permission.repository;

import com.hoxkar.permission.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 角色Repository
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据租户ID和角色代码查找角色
     */
    Optional<Role> findByTenantIdAndRoleCode(Long tenantId, String roleCode);
    
    /**
     * 根据租户ID查找所有角色
     */
    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId")
    List<Role> findAllByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 根据租户ID分页查找角色
     */
    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId")
    Page<Role> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    /**
     * 根据租户ID和状态查找角色
     */
    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId AND r.status = :status")
    List<Role> findByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status);
    
    /**
     * 根据租户ID和角色名称模糊查询
     */
    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId AND r.roleName LIKE %:roleName%")
    List<Role> findByTenantIdAndRoleNameContaining(@Param("tenantId") Long tenantId, @Param("roleName") String roleName);
    
    /**
     * 检查租户ID和角色代码是否存在
     */
    boolean existsByTenantIdAndRoleCode(Long tenantId, String roleCode);
    
    /**
     * 根据用户ID查找角色
     */
    @Query("SELECT r FROM Role r JOIN UserRole ur ON r.id = ur.roleId WHERE ur.tenantId = :tenantId AND ur.userId = :userId")
    List<Role> findByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    // 分页查询
    Page<Role> findByTenantIdAndRoleNameContaining(Long tenantId, String roleName, Pageable pageable);
    Page<Role> findByTenantIdAndRoleCodeContaining(Long tenantId, String roleCode, Pageable pageable);
    Page<Role> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);
    Page<Role> findByTenantIdAndIsSystem(Long tenantId, Boolean isSystem, Pageable pageable);

    // 统计方法
    long countByTenantId(Long tenantId);
    long countByTenantIdAndStatus(Long tenantId, String status);
    long countByTenantIdAndIsSystem(Long tenantId, boolean isSystem);

    // 统计方法（ServiceImpl兼容）
    @Query("SELECT COUNT(r) FROM Role r WHERE r.tenantId = :tenantId AND r.status = 'ACTIVE'")
    long countByTenantIdAndStatusActive(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(r) FROM Role r WHERE r.tenantId = :tenantId AND r.status = 'INACTIVE'")
    long countByTenantIdAndStatusInactive(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(r) FROM Role r WHERE r.tenantId = :tenantId AND r.isSystem = true")
    long countByTenantIdAndIsSystemTrue(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(r) FROM Role r WHERE r.tenantId = :tenantId AND r.isSystem = false")
    long countByTenantIdAndIsSystemFalse(@Param("tenantId") Long tenantId);
} 