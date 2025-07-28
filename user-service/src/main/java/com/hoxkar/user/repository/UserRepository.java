package com.hoxkar.user.repository;

import com.hoxkar.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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
    List<User> findAllByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 根据租户ID分页查找用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId")
    Page<User> findAllByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    /**
     * 根据租户ID和状态查找用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = :status")
    List<User> findByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status);
    
    /**
     * 根据租户ID和状态分页查找用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.status = :status")
    Page<User> findByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status, Pageable pageable);
    
    /**
     * 根据租户ID和真实姓名模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.realName LIKE %:realName%")
    List<User> findByTenantIdAndRealNameContaining(@Param("tenantId") Long tenantId, @Param("realName") String realName);
    
    /**
     * 根据租户ID和真实姓名模糊查询分页
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.realName LIKE %:realName%")
    Page<User> findByTenantIdAndRealNameContaining(@Param("tenantId") Long tenantId, @Param("realName") String realName, Pageable pageable);
    
    /**
     * 根据租户ID和是否为超级管理员查找用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.isSuperAdmin = :isSuperAdmin")
    List<User> findByTenantIdAndIsSuperAdmin(@Param("tenantId") Long tenantId, @Param("isSuperAdmin") Boolean isSuperAdmin);
    
    /**
     * 根据租户ID和是否为超级管理员分页查找用户
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.isSuperAdmin = :isSuperAdmin")
    Page<User> findByTenantIdAndIsSuperAdmin(@Param("tenantId") Long tenantId, @Param("isSuperAdmin") Boolean isSuperAdmin, Pageable pageable);
    
    /**
     * 根据租户ID和用户名模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.username LIKE %:username%")
    List<User> findByTenantIdAndUsernameContaining(@Param("tenantId") Long tenantId, @Param("username") String username);
    
    /**
     * 根据租户ID和用户名模糊查询分页
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.username LIKE %:username%")
    Page<User> findByTenantIdAndUsernameContaining(@Param("tenantId") Long tenantId, @Param("username") String username, Pageable pageable);
    
    /**
     * 根据租户ID和邮箱模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.email LIKE %:email%")
    List<User> findByTenantIdAndEmailContaining(@Param("tenantId") Long tenantId, @Param("email") String email);
    
    /**
     * 根据租户ID和邮箱模糊查询分页
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.email LIKE %:email%")
    Page<User> findByTenantIdAndEmailContaining(@Param("tenantId") Long tenantId, @Param("email") String email, Pageable pageable);
    
    /**
     * 根据租户ID和手机号模糊查询
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.phone LIKE %:phone%")
    List<User> findByTenantIdAndPhoneContaining(@Param("tenantId") Long tenantId, @Param("phone") String phone);
    
    /**
     * 根据租户ID和手机号模糊查询分页
     */
    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.phone LIKE %:phone%")
    Page<User> findByTenantIdAndPhoneContaining(@Param("tenantId") Long tenantId, @Param("phone") String phone, Pageable pageable);
    
    /**
     * 统计租户下的用户总数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId")
    Long countByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 统计租户下活跃用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.status = 'ACTIVE'")
    Long countByTenantIdAndStatusActive(@Param("tenantId") Long tenantId);
    
    /**
     * 统计租户下禁用用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.status = 'INACTIVE'")
    Long countByTenantIdAndStatusInactive(@Param("tenantId") Long tenantId);
    
    /**
     * 统计租户下超级管理员数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.isSuperAdmin = true")
    Long countByTenantIdAndIsSuperAdminTrue(@Param("tenantId") Long tenantId);
    
    /**
     * 统计租户下普通用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.isSuperAdmin = false")
    Long countByTenantIdAndIsSuperAdminFalse(@Param("tenantId") Long tenantId);
    
    /**
     * 统计租户下本月新增用户数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.createdAt >= :startDate")
    Long countByTenantIdAndCreatedAtAfter(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate);
    
    /**
     * 统计租户下本月活跃用户数（有最后登录时间）
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenantId = :tenantId AND u.lastLoginAt >= :startDate")
    Long countByTenantIdAndLastLoginAtAfter(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate);
} 