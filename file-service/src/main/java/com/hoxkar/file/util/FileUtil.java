package com.hoxkar.file.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件工具类
 */
@Slf4j
@Component
public class FileUtil {
    
    /**
     * 图片文件类型
     */
    private static final List<String> IMAGE_TYPES = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp"
    );
    
    /**
     * 文档文件类型
     */
    private static final List<String> DOCUMENT_TYPES = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
    );
    
    /**
     * 压缩文件类型
     */
    private static final List<String> ARCHIVE_TYPES = Arrays.asList(
            "zip", "rar", "7z", "tar", "gz"
    );
    
    /**
     * 生成文件存储路径
     */
    public static String generateFilePath(String rootPath, String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        
        // 按年月日组织目录结构
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        return Paths.get(rootPath, datePath, fileName).toString();
    }
    
    /**
     * 生成缩略图路径
     */
    public static String generateThumbnailPath(String thumbnailPath, String filePath) {
        String fileName = FilenameUtils.getName(filePath);
        String nameWithoutExt = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        
        return Paths.get(thumbnailPath, nameWithoutExt + "_thumb." + extension).toString();
    }
    
    /**
     * 创建目录
     */
    public static void createDirectories(String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }
    
    /**
     * 检查文件类型是否允许
     */
    public static boolean isAllowedFileType(String filename, List<String> allowedTypes) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return allowedTypes.contains(extension);
    }
    
    /**
     * 检查文件大小是否在限制范围内
     */
    public static boolean isFileSizeValid(long fileSize, long maxSizeMB) {
        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        return fileSize <= maxSizeBytes;
    }
    
    /**
     * 获取文件类型分类
     */
    public static String getFileTypeCategory(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        
        if (IMAGE_TYPES.contains(extension)) {
            return "IMAGE";
        } else if (DOCUMENT_TYPES.contains(extension)) {
            return "DOCUMENT";
        } else if (ARCHIVE_TYPES.contains(extension)) {
            return "ARCHIVE";
        } else {
            return "OTHER";
        }
    }
    
    /**
     * 计算文件MD5哈希值
     */
    public static String calculateFileHash(File file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            byte[] hashBytes = md.digest(fileBytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5算法不可用", e);
        }
    }
    
    /**
     * 计算MultipartFile的MD5哈希值
     */
    public static String calculateFileHash(MultipartFile file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] fileBytes = file.getBytes();
            byte[] hashBytes = md.digest(fileBytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("MD5算法不可用", e);
        }
    }
    
    /**
     * 生成缩略图
     */
    public static void generateThumbnail(String sourcePath, String targetPath, int width, int height, double quality) throws IOException {
        try {
            Thumbnails.of(sourcePath)
                    .size(width, height)
                    .outputQuality(quality)
                    .toFile(targetPath);
        } catch (IOException e) {
            log.error("生成缩略图失败: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 检查是否为图片文件
     */
    public static boolean isImageFile(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return IMAGE_TYPES.contains(extension);
    }
    
    /**
     * 获取文件大小的可读格式
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists() && file.delete();
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取MIME类型
     */
    public static String getMimeType(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            case "zip":
                return "application/zip";
            default:
                return "application/octet-stream";
        }
    }
} 