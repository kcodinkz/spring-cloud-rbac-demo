package com.hoxkar.file.dto;

import lombok.Data;

import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * 文件搜索请求
 */
@Data
public class FileSearchRequest {
    
    /**
     * 文件名关键词
     */
    private String fileName;
    
    /**
     * 原始文件名关键词
     */
    private String originalName;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件类型分类
     */
    private String fileTypeCategory;
    
    /**
     * 文件状态
     */
    private String status;
    
    /**
     * 上传者ID
     */
    private Long uploaderId;
    
    /**
     * 描述关键词
     */
    private String description;
    
    /**
     * 标签关键词
     */
    private String tags;
    
    /**
     * 最小文件大小 (字节)
     */
    private Long minFileSize;
    
    /**
     * 最大文件大小 (字节)
     */
    private Long maxFileSize;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 页码 (从0开始)
     */
    @Min(0)
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    @Min(1)
    private Integer size = 20;
    
    /**
     * 排序字段
     */
    private String sortBy = "createdAt";
    
    /**
     * 排序方向 (asc, desc)
     */
    private String sortDirection = "desc";
} 