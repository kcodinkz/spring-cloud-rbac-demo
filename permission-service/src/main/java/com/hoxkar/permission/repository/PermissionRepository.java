package com.hoxkar.permission.repository;

import com.hoxkar.permission.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 权限Repository
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    /**
     * 根据租户ID和权限代码查找权限
     */
    Optional<Permission> findByTenantIdAndPermissionCode(Long tenantId, String permissionCode);
    
    /**
     * 根据租户ID查找所有权限
     */
    @Query("SELECT p FROM Permission p WHERE p.tenantId = :tenantId")
    List<Permission> findAllByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 根据租户ID分页查找权限
     */
    @Query("SELECT p FROM Permission p WHERE p.tenantId = :tenantId")
    Page<Permission> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    /**
     * 根据租户ID和权限类型查找权限
     */
    @Query("SELECT p FROM Permission p WHERE p.tenantId = :tenantId AND p.permissionType = :permissionType")
    List<Permission> findByTenantIdAndPermissionType(@Param("tenantId") Long tenantId, @Param("permissionType") String permissionType);
    
    /**
     * 根据租户ID和父级ID查找权限
     */
    @Query("SELECT p FROM Permission p WHERE p.tenantId = :tenantId AND p.parentId = :parentId")
    List<Permission> findByTenantIdAndParentId(@Param("tenantId") Long tenantId, @Param("parentId") Long parentId);
    
    /**
     * 根据租户ID和状态查找权限
     */
    @Query("SELECT p FROM Permission p WHERE p.tenantId = :tenantId AND p.status = :status")
    List<Permission> findByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status);
    
    /**
     * 根据租户ID和权限名称模糊查询
     */
    @Query("SELECT p FROM Permission p WHERE p.tenantId = :tenantId AND p.permissionName LIKE %:permissionName%")
    List<Permission> findByTenantIdAndPermissionNameContaining(@Param("tenantId") Long tenantId, @Param("permissionName") String permissionName);
    
    /**
     * 检查租户ID和权限代码是否存在
     */
    boolean existsByTenantIdAndPermissionCode(Long tenantId, String permissionCode);
    
    /**
     * 根据角色ID查找权限
     */
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.tenantId = :tenantId AND r.id = :roleId")
    List<Permission> findByRoleId(@Param("tenantId") Long tenantId, @Param("roleId") Long roleId);
    
    /**
     * 根据用户ID查找权限
     */
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.roles r JOIN UserRole ur ON r.id = ur.roleId WHERE ur.tenantId = :tenantId AND ur.userId = :userId")
    List<Permission> findByUserId(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    // 分页查询
    Page<Permission> findByTenantIdAndPermissionNameContaining(Long tenantId, String permissionName, Pageable pageable);
    Page<Permission> findByTenantIdAndPermissionCodeContaining(Long tenantId, String permissionCode, Pageable pageable);
    Page<Permission> findByTenantIdAndPermissionType(Long tenantId, String permissionType, Pageable pageable);
    Page<Permission> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);
    Page<Permission> findByTenantIdAndIsSystem(Long tenantId, Boolean isSystem, Pageable pageable);

    // 统计方法
    long countByTenantId(Long tenantId);
    long countByTenantIdAndStatus(Long tenantId, String status);
    long countByTenantIdAndIsSystem(Long tenantId, boolean isSystem);
    long countByTenantIdAndPermissionType(Long tenantId, String permissionType);
    // 统计方法（ServiceImpl兼容）
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.tenantId = :tenantId AND p.status = 'ACTIVE'")
    long countByTenantIdAndStatusActive(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.tenantId = :tenantId AND p.status = 'INACTIVE'")
    long countByTenantIdAndStatusInactive(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.tenantId = :tenantId AND p.isSystem = true")
    long countByTenantIdAndIsSystemTrue(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(p) FROM Permission p WHERE p.tenantId = :tenantId AND p.isSystem = false")
    long countByTenantIdAndIsSystemFalse(@Param("tenantId") Long tenantId);
} 