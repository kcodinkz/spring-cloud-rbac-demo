package com.hoxkar.file.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件信息VO
 */
@Data
public class FileVO {
    
    /**
     * 文件ID
     */
    private Long id;
    
    /**
     * 租户ID
     */
    private String tenantId;
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 原始文件名
     */
    private String originalName;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件大小 (字节)
     */
    private Long fileSize;
    
    /**
     * 文件大小的可读格式
     */
    private String readableFileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件类型分类
     */
    private String fileTypeCategory;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * MIME类型
     */
    private String mimeType;
    
    /**
     * 文件哈希值
     */
    private String fileHash;
    
    /**
     * 缩略图路径
     */
    private String thumbnailPath;
    
    /**
     * 缩略图URL
     */
    private String thumbnailUrl;
    
    /**
     * 文件状态
     */
    private String status;
    
    /**
     * 上传者ID
     */
    private Long uploaderId;
    
    /**
     * 上传者名称
     */
    private String uploaderName;
    
    /**
     * 文件描述
     */
    private String description;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 删除时间
     */
    private LocalDateTime deletedAt;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 下载URL
     */
    private String downloadUrl;
    
    /**
     * 预览URL
     */
    private String previewUrl;
} 