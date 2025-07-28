package com.hoxkar.file.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件信息实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "file_info")
@EntityListeners(AuditingEntityListener.class)
public class FileInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;
    
    /**
     * 文件路径
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    /**
     * 文件大小 (字节)
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    /**
     * 文件类型
     */
    @Column(name = "file_type", length = 50)
    private String fileType;
    
    /**
     * 文件扩展名
     */
    @Column(name = "file_extension", length = 20)
    private String fileExtension;
    
    /**
     * MIME类型
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    /**
     * 文件哈希值 (MD5)
     */
    @Column(name = "file_hash", length = 32)
    private String fileHash;
    
    /**
     * 缩略图路径
     */
    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;
    
    /**
     * 文件状态 (ACTIVE, DELETED)
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "ACTIVE";
    
    /**
     * 上传者ID
     */
    @Column(name = "uploader_id")
    private Long uploaderId;
    
    /**
     * 上传者名称
     */
    @Column(name = "uploader_name", length = 100)
    private String uploaderName;
    
    /**
     * 文件描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 标签
     */
    @Column(name = "tags", length = 500)
    private String tags;
    
    /**
     * 下载次数
     */
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;
    
    /**
     * 查看次数
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 删除时间
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
} 