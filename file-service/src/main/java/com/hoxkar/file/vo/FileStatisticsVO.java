package com.hoxkar.file.vo;

import lombok.Data;

import java.util.Map;

/**
 * 文件统计信息VO
 */
@Data
public class FileStatisticsVO {
    
    /**
     * 总文件数
     */
    private Long totalFiles;
    
    /**
     * 活跃文件数
     */
    private Long activeFiles;
    
    /**
     * 已删除文件数
     */
    private Long deletedFiles;
    
    /**
     * 总文件大小 (字节)
     */
    private Long totalFileSize;
    
    /**
     * 总文件大小的可读格式
     */
    private String readableTotalFileSize;
    
    /**
     * 按文件类型统计
     */
    private Map<String, Long> filesByType;
    
    /**
     * 按文件类型分类统计
     */
    private Map<String, Long> filesByCategory;
    
    /**
     * 按文件类型统计的文件大小
     */
    private Map<String, Long> fileSizeByType;
    
    /**
     * 按文件类型分类统计的文件大小
     */
    private Map<String, Long> fileSizeByCategory;
    
    /**
     * 本月上传文件数
     */
    private Long filesUploadedThisMonth;
    
    /**
     * 本月上传文件大小
     */
    private Long fileSizeUploadedThisMonth;
    
    /**
     * 本月上传文件大小的可读格式
     */
    private String readableFileSizeUploadedThisMonth;
    
    /**
     * 今日上传文件数
     */
    private Long filesUploadedToday;
    
    /**
     * 今日上传文件大小
     */
    private Long fileSizeUploadedToday;
    
    /**
     * 今日上传文件大小的可读格式
     */
    private String readableFileSizeUploadedToday;
    
    /**
     * 总下载次数
     */
    private Long totalDownloads;
    
    /**
     * 总查看次数
     */
    private Long totalViews;
    
    /**
     * 平均文件大小
     */
    private Long averageFileSize;
    
    /**
     * 平均文件大小的可读格式
     */
    private String readableAverageFileSize;
} 