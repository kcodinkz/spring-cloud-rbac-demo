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

// File Utility Class
/**
 * File utility class // 文件工具类
 */
@Slf4j
@Component
public class FileUtil {
    
    /**
     * Image file types
     */
    private static final List<String> IMAGE_TYPES = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp"
    );
    
    /**
     * Document file types
     */
    private static final List<String> DOCUMENT_TYPES = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"
    );
    
    /**
     * Archive file types
     */
    private static final List<String> ARCHIVE_TYPES = Arrays.asList(
            "zip", "rar", "7z", "tar", "gz"
    );
    
    /**
     * Generate file storage path
     */
    public static String generateFilePath(String rootPath, String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        
        // Organize directory structure by year, month, and day
        LocalDateTime now = LocalDateTime.now();
        String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        
        return Paths.get(rootPath, datePath, fileName).toString();
    }
    
    /**
     * Generate thumbnail path
     */
    public static String generateThumbnailPath(String thumbnailPath, String filePath) {
        String fileName = FilenameUtils.getName(filePath);
        String nameWithoutExt = FilenameUtils.getBaseName(fileName);
        String extension = FilenameUtils.getExtension(fileName);
        
        return Paths.get(thumbnailPath, nameWithoutExt + "_thumb." + extension).toString();
    }
    
    /**
     * Create directories
     */
    public static void createDirectories(String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }
    
    /**
     * Check if file type is allowed
     */
    public static boolean isAllowedFileType(String filename, List<String> allowedTypes) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return allowedTypes.contains(extension);
    }
    
    /**
     * Check if file size is within limits
     */
    public static boolean isFileSizeValid(long fileSize, long maxSizeMB) {
        long maxSizeBytes = maxSizeMB * 1024 * 1024;
        return fileSize <= maxSizeBytes;
    }
    
    /**
     * Get file type category
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
     * Calculate file MD5 hash
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
            throw new IOException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * Calculate MD5 hash for MultipartFile
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
            throw new IOException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * Generate thumbnail
     */
    public static void generateThumbnail(String sourcePath, String targetPath, int width, int height, double quality) throws IOException {
        try {
            Thumbnails.of(sourcePath)
                    .size(width, height)
                    .outputQuality(quality)
                    .toFile(targetPath);
        } catch (IOException e) {
            log.error("Failed to generate thumbnail: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Check if it's an image file
     */
    public static boolean isImageFile(String filename) {
        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return IMAGE_TYPES.contains(extension);
    }
    
    /**
     * Get readable file size
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    
    /**
     * Delete file
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists() && file.delete();
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get MIME type
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