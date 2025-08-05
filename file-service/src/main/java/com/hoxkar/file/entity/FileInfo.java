package com.hoxkar.file.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// File Info Entity
/**
 * File info entity // 文件信息实体
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
     * Tenant ID // 租户ID
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    /**
     * File name // 文件名
     */
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    /**
     * Original file name // 原始文件名
     */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;
    /**
     * File path // 文件路径
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    /**
     * File size (bytes) // 文件大小 (字节)
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    /**
     * File type // 文件类型
     */
    @Column(name = "file_type", length = 50)
    private String fileType;
    /**
     * File extension // 文件扩展名
     */
    @Column(name = "file_extension", length = 20)
    private String fileExtension;
    /**
     * MIME type // MIME类型
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    /**
     * File hash (MD5) // 文件哈希值 (MD5)
     */
    @Column(name = "file_hash", length = 32)
    private String fileHash;
    /**
     * Thumbnail path // 缩略图路径
     */
    @Column(name = "thumbnail_path", length = 500)
    private String thumbnailPath;
    /**
     * File status (ACTIVE, DELETED) // 文件状态 (ACTIVE, DELETED)
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status = "ACTIVE";
    /**
     * Uploader ID // 上传者ID
     */
    @Column(name = "uploader_id")
    private Long uploaderId;
    /**
     * Uploader name // 上传者名称
     */
    @Column(name = "uploader_name", length = 100)
    private String uploaderName;
    /**
     * File description // 文件描述
     */
    @Column(name = "description", length = 500)
    private String description;
    /**
     * Tags // 标签
     */
    @Column(name = "tags", length = 500)
    private String tags;
    /**
     * Download count // 下载次数
     */
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;
    /**
     * View count // 查看次数
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;
    /**
     * Created time // 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    /**
     * Updated time // 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    /**
     * Deleted time // 删除时间
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
} 