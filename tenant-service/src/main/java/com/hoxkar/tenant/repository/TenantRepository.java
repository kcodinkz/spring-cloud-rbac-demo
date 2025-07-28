package com.hoxkar.tenant.repository;

import com.hoxkar.tenant.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 租户Repository
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    
    /**
     * 根据租户代码查找租户
     */
    Optional<Tenant> findByTenantCode(String tenantCode);
    
    /**
     * 根据域名查找租户
     */
    Optional<Tenant> findByDomain(String domain);
    
    /**
     * 根据状态查找租户
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = :status")
    List<Tenant> findByStatus(@Param("status") String status);
    
    /**
     * 根据状态分页查找租户
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = :status")
    Page<Tenant> findByStatus(@Param("status") String status, Pageable pageable);
    
    /**
     * 根据订阅计划查找租户
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionPlan = :subscriptionPlan")
    List<Tenant> findBySubscriptionPlan(@Param("subscriptionPlan") String subscriptionPlan);
    
    /**
     * 根据租户名称模糊查询
     */
    @Query("SELECT t FROM Tenant t WHERE t.tenantName LIKE %:tenantName%")
    List<Tenant> findByTenantNameContaining(@Param("tenantName") String tenantName);
    
    /**
     * 根据租户名称模糊查询分页
     */
    @Query("SELECT t FROM Tenant t WHERE t.tenantName LIKE %:tenantName%")
    Page<Tenant> findByTenantNameContaining(@Param("tenantName") String tenantName, Pageable pageable);
    
    /**
     * 根据联系人邮箱查找租户
     */
    Optional<Tenant> findByContactEmail(String contactEmail);
    
    /**
     * 检查租户代码是否存在
     */
    boolean existsByTenantCode(String tenantCode);
    
    /**
     * 检查域名是否存在
     */
    boolean existsByDomain(String domain);
    
    /**
     * 检查联系人邮箱是否存在
     */
    boolean existsByContactEmail(String contactEmail);
    
    /**
     * 查找即将过期的租户
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionEndDate <= :endDate AND t.status = 'ACTIVE'")
    List<Tenant> findExpiringTenants(@Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * 查找已过期的租户
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionEndDate < :currentDate AND t.status = 'ACTIVE'")
    List<Tenant> findExpiredTenants(@Param("currentDate") java.time.LocalDateTime currentDate);
    
    /**
     * 统计各状态的租户数量
     */
    @Query("SELECT t.status, COUNT(t) FROM Tenant t GROUP BY t.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计各订阅计划的租户数量
     */
    @Query("SELECT t.subscriptionPlan, COUNT(t) FROM Tenant t WHERE t.subscriptionPlan IS NOT NULL GROUP BY t.subscriptionPlan")
    List<Object[]> countBySubscriptionPlan();
    
    // 补充ServiceImpl用到的分页查询方法
    /**
     * 根据租户代码模糊查询分页
     */
    @Query("SELECT t FROM Tenant t WHERE t.tenantCode LIKE %:tenantCode%")
    Page<Tenant> findByTenantCodeContaining(@Param("tenantCode") String tenantCode, Pageable pageable);
    
    /**
     * 根据域名模糊查询分页
     */
    @Query("SELECT t FROM Tenant t WHERE t.domain LIKE %:domain%")
    Page<Tenant> findByDomainContaining(@Param("domain") String domain, Pageable pageable);
    
    /**
     * 根据联系人邮箱模糊查询分页
     */
    @Query("SELECT t FROM Tenant t WHERE t.contactEmail LIKE %:contactEmail%")
    Page<Tenant> findByContactEmailContaining(@Param("contactEmail") String contactEmail, Pageable pageable);
    
    /**
     * 根据订阅计划分页查询
     */
    @Query("SELECT t FROM Tenant t WHERE t.subscriptionPlan = :subscriptionPlan")
    Page<Tenant> findBySubscriptionPlan(@Param("subscriptionPlan") String subscriptionPlan, Pageable pageable);
    
    // 补充ServiceImpl用到的统计方法
    /**
     * 根据状态统计租户数量
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.status = :status")
    long countByStatus(@Param("status") String status);
    
    /**
     * 根据订阅计划统计租户数量
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.subscriptionPlan = :subscriptionPlan")
    long countBySubscriptionPlan(@Param("subscriptionPlan") String subscriptionPlan);
    
    /**
     * 统计即将过期的租户数量
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.subscriptionEndDate <= :endDate AND t.status = 'ACTIVE'")
    long countExpiringTenants(@Param("endDate") java.time.LocalDateTime endDate);
    
    /**
     * 统计已过期的租户数量
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.subscriptionEndDate < :currentDate AND t.status = 'ACTIVE'")
    long countExpiredTenants(@Param("currentDate") java.time.LocalDateTime currentDate);
    
    /**
     * 统计当前用户总数
     */
    @Query("SELECT SUM(t.currentUsers) FROM Tenant t WHERE t.currentUsers IS NOT NULL")
    Long sumCurrentUsers();
    
    /**
     * 根据创建时间统计租户数量
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.createdAt >= :createdAfter")
    long countByCreatedAtAfter(@Param("createdAfter") java.time.LocalDateTime createdAfter);
    

} 