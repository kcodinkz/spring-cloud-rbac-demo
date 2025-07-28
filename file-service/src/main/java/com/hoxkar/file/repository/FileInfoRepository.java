package com.hoxkar.file.repository;

import com.hoxkar.file.entity.FileInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件信息仓库接口
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    
    /**
     * 根据租户ID查找所有文件
     */
    List<FileInfo> findByTenantId(Long tenantId);
    
    /**
     * 根据租户ID分页查找文件
     */
    Page<FileInfo> findByTenantId(Long tenantId, Pageable pageable);
    
    /**
     * 根据租户ID和状态查找文件
     */
    List<FileInfo> findByTenantIdAndStatus(Long tenantId, String status);
    
    /**
     * 根据租户ID和状态分页查找文件
     */
    Page<FileInfo> findByTenantIdAndStatus(Long tenantId, String status, Pageable pageable);
    
    /**
     * 根据租户ID和文件类型查找文件
     */
    List<FileInfo> findByTenantIdAndFileType(Long tenantId, String fileType);
    
    /**
     * 根据租户ID和文件类型分页查找文件
     */
    Page<FileInfo> findByTenantIdAndFileType(Long tenantId, String fileType, Pageable pageable);
    
    /**
     * 根据租户ID和上传者ID查找文件
     */
    List<FileInfo> findByTenantIdAndUploaderId(Long tenantId, Long uploaderId);
    
    /**
     * 根据租户ID和上传者ID分页查找文件
     */
    Page<FileInfo> findByTenantIdAndUploaderId(Long tenantId, Long uploaderId, Pageable pageable);
    
    /**
     * 根据租户ID和文件名模糊查找
     */
    List<FileInfo> findByTenantIdAndFileNameContaining(Long tenantId, String fileName);
    
    /**
     * 根据租户ID和文件名模糊查找分页
     */
    Page<FileInfo> findByTenantIdAndFileNameContaining(Long tenantId, String fileName, Pageable pageable);
    
    /**
     * 根据租户ID和原始文件名模糊查找
     */
    List<FileInfo> findByTenantIdAndOriginalNameContaining(Long tenantId, String originalName);
    
    /**
     * 根据租户ID和原始文件名模糊查找分页
     */
    Page<FileInfo> findByTenantIdAndOriginalNameContaining(Long tenantId, String originalName, Pageable pageable);
    
    /**
     * 根据租户ID和描述模糊查找
     */
    List<FileInfo> findByTenantIdAndDescriptionContaining(Long tenantId, String description);
    
    /**
     * 根据租户ID和描述模糊查找分页
     */
    Page<FileInfo> findByTenantIdAndDescriptionContaining(Long tenantId, String description, Pageable pageable);
    
    /**
     * 根据租户ID和标签模糊查找
     */
    List<FileInfo> findByTenantIdAndTagsContaining(Long tenantId, String tags);
    
    /**
     * 根据租户ID和标签模糊查找分页
     */
    Page<FileInfo> findByTenantIdAndTagsContaining(Long tenantId, String tags, Pageable pageable);
    
    /**
     * 根据租户ID和创建时间范围查找
     */
    List<FileInfo> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据租户ID和创建时间范围分页查找
     */
    Page<FileInfo> findByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据租户ID和文件大小范围查找
     */
    List<FileInfo> findByTenantIdAndFileSizeBetween(Long tenantId, Long minSize, Long maxSize);
    
    /**
     * 根据租户ID和文件大小范围分页查找
     */
    Page<FileInfo> findByTenantIdAndFileSizeBetween(Long tenantId, Long minSize, Long maxSize, Pageable pageable);
    
    /**
     * 根据文件哈希值查找
     */
    Optional<FileInfo> findByFileHash(String fileHash);
    
    /**
     * 根据租户ID和文件哈希值查找
     */
    Optional<FileInfo> findByTenantIdAndFileHash(Long tenantId, String fileHash);
    
    /**
     * 根据租户ID和文件名查找
     */
    Optional<FileInfo> findByTenantIdAndFileName(Long tenantId, String fileName);
    
    /**
     * 根据租户ID统计文件总数
     */
    Long countByTenantId(Long tenantId);
    
    /**
     * 根据租户ID和状态统计文件数量
     */
    Long countByTenantIdAndStatus(Long tenantId, String status);
    
    /**
     * 根据租户ID和文件类型统计文件数量
     */
    Long countByTenantIdAndFileType(Long tenantId, String fileType);
    
    /**
     * 根据租户ID和上传者ID统计文件数量
     */
    Long countByTenantIdAndUploaderId(Long tenantId, Long uploaderId);
    
    /**
     * 根据租户ID和创建时间范围统计文件数量
     */
    Long countByTenantIdAndCreatedAtBetween(Long tenantId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据租户ID统计总文件大小
     */
    @Query("SELECT SUM(f.fileSize) FROM FileInfo f WHERE f.tenantId = :tenantId AND f.status = 'ACTIVE'")
    Long sumFileSizeByTenantId(@Param("tenantId") Long tenantId);
    
    /**
     * 根据租户ID和文件类型统计总文件大小
     */
    @Query("SELECT SUM(f.fileSize) FROM FileInfo f WHERE f.tenantId = :tenantId AND f.fileType = :fileType AND f.status = 'ACTIVE'")
    Long sumFileSizeByTenantIdAndFileType(@Param("tenantId") Long tenantId, @Param("fileType") String fileType);
    
    /**
     * 根据租户ID查找最近上传的文件
     */
    @Query("SELECT f FROM FileInfo f WHERE f.tenantId = :tenantId AND f.status = 'ACTIVE' ORDER BY f.createdAt DESC")
    List<FileInfo> findRecentFilesByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    /**
     * 根据租户ID查找最受欢迎的文件（按下载次数排序）
     */
    @Query("SELECT f FROM FileInfo f WHERE f.tenantId = :tenantId AND f.status = 'ACTIVE' ORDER BY f.downloadCount DESC")
    List<FileInfo> findPopularFilesByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
    
    /**
     * 根据租户ID查找最大的文件（按文件大小排序）
     */
    @Query("SELECT f FROM FileInfo f WHERE f.tenantId = :tenantId AND f.status = 'ACTIVE' ORDER BY f.fileSize DESC")
    List<FileInfo> findLargestFilesByTenantId(@Param("tenantId") Long tenantId, Pageable pageable);
} 