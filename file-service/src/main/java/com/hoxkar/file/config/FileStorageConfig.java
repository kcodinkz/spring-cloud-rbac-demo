package com.hoxkar.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 文件存储配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageConfig {
    
    /**
     * 文件存储根目录
     */
    private String rootPath = "./uploads";
    
    /**
     * 临时文件目录
     */
    private String tempPath = "./temp";
    
    /**
     * 缩略图目录
     */
    private String thumbnailPath = "./thumbnails";
    
    /**
     * 允许的文件类型
     */
    private List<String> allowedTypes;
    
    /**
     * 最大文件大小 (MB)
     */
    private Long maxSize = 100L;
    
    /**
     * 缩略图配置
     */
    private ThumbnailConfig thumbnail = new ThumbnailConfig();
    
    @Data
    public static class ThumbnailConfig {
        /**
         * 缩略图宽度
         */
        private Integer width = 200;
        
        /**
         * 缩略图高度
         */
        private Integer height = 200;
        
        /**
         * 缩略图质量 (0.0-1.0)
         */
        private Double quality = 0.8;
    }
} 