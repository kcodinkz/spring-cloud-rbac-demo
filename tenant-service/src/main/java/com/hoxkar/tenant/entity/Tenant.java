package com.hoxkar.tenant.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenants")
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_code", nullable = false, unique = true)
    private String tenantCode;
    
    @Column(name = "tenant_name", nullable = false)
    private String tenantName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "contact_person")
    private String contactPerson;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "domain")
    private String domain;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";
    
    @Column(name = "max_users")
    private Integer maxUsers;
    
    @Column(name = "current_users")
    private Integer currentUsers = 0;
    
    @Column(name = "subscription_plan")
    private String subscriptionPlan;
    
    @Column(name = "subscription_start_date")
    private LocalDateTime subscriptionStartDate;
    
    @Column(name = "subscription_end_date")
    private LocalDateTime subscriptionEndDate;
    
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
} 