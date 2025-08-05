package com.hoxkar.file.service.impl;

import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.common.exception.BusinessException;
import com.hoxkar.common.util.TenantContext;
import com.hoxkar.file.config.FileStorageConfig;
import com.hoxkar.file.dto.FileSearchRequest;
import com.hoxkar.file.entity.FileInfo;
import com.hoxkar.file.repository.FileInfoRepository;
import com.hoxkar.file.service.FileService;
import com.hoxkar.file.util.FileUtil;
import com.hoxkar.file.vo.FileStatisticsVO;
import com.hoxkar.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// File Service Implementation
/**
 * 文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    
    private final FileInfoRepository fileInfoRepository;
    private final FileStorageConfig fileStorageConfig;
    
    @Override
    @Transactional
    public ApiResponse<FileVO> uploadFile(MultipartFile file, String description, String tags, Long uploaderId, String uploaderName) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Validate file
            validateFile(file);
            
            // Generate file path
            String filePath = FileUtil.generateFilePath(fileStorageConfig.getRootPath(), file.getOriginalFilename());
            
            // Create directory
            FileUtil.createDirectories(FilenameUtils.getFullPath(filePath));
            
            // Save file
            File savedFile = new File(filePath);
            file.transferTo(savedFile);
            
            // Calculate file hash
            String fileHash = FileUtil.calculateFileHash(file);
            
            // Check if file exists
            Optional<FileInfo> existingFile = fileInfoRepository.findByTenantIdAndFileHash(Long.valueOf(tenantId), fileHash);
            if (existingFile.isPresent()) {
                // Delete the recently saved file
                FileUtil.deleteFile(filePath);
                throw BusinessException.of("File already exists");
            }
            
            // Generate thumbnail (if it's an image)
            String thumbnailPath = null;
            if (FileUtil.isImageFile(file.getOriginalFilename())) {
                thumbnailPath = FileUtil.generateThumbnailPath(fileStorageConfig.getThumbnailPath(), filePath);
                FileUtil.createDirectories(FilenameUtils.getFullPath(thumbnailPath));
                FileUtil.generateThumbnail(filePath, thumbnailPath, 
                    fileStorageConfig.getThumbnail().getWidth(), 
                    fileStorageConfig.getThumbnail().getHeight(), 
                    fileStorageConfig.getThumbnail().getQuality());
            }
            
            // Create file info
            FileInfo fileInfo = new FileInfo();
            fileInfo.setTenantId(Long.valueOf(tenantId));
            fileInfo.setFileName(FilenameUtils.getName(filePath));
            fileInfo.setOriginalName(file.getOriginalFilename());
            fileInfo.setFilePath(filePath);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(FilenameUtils.getExtension(file.getOriginalFilename()));
            fileInfo.setFileExtension(FilenameUtils.getExtension(file.getOriginalFilename()));
            fileInfo.setMimeType(FileUtil.getMimeType(file.getOriginalFilename()));
            fileInfo.setFileHash(fileHash);
            fileInfo.setThumbnailPath(thumbnailPath);
            fileInfo.setUploaderId(uploaderId);
            fileInfo.setUploaderName(uploaderName);
            fileInfo.setDescription(description);
            fileInfo.setTags(tags);
            fileInfo.setStatus("ACTIVE");
            
            // Save file info
            FileInfo savedFileInfo = fileInfoRepository.save(fileInfo);
            
            log.info("File uploaded successfully: {}", savedFileInfo.getOriginalName());
            return ApiResponse.success("File uploaded successfully", convertToVO(savedFileInfo));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("File upload failed: ", e);
            throw BusinessException.of("File upload failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<List<FileVO>> uploadFiles(MultipartFile[] files, String description, String tags, Long uploaderId, String uploaderName) {
        try {
            List<FileVO> uploadedFiles = new ArrayList<>();
            
            for (MultipartFile file : files) {
                try {
                    ApiResponse<FileVO> response = uploadFile(file, description, tags, uploaderId, uploaderName);
                    if (response.getCode() == 200) {
                        uploadedFiles.add(response.getData());
                    }
                } catch (Exception e) {
                    log.error("Failed to upload single file during batch upload: {}", file.getOriginalFilename(), e);
                }
            }
            
            return ApiResponse.success("Batch upload completed", uploadedFiles);
            
        } catch (Exception e) {
            log.error("Batch upload files failed: ", e);
            throw BusinessException.of("Batch upload files failed");
        }
    }
    
    @Override
    public void downloadFile(Long fileId, HttpServletResponse response) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to download this file");
            }
            
            // Check if file exists
            File file = new File(fileInfo.getFilePath());
            if (!file.exists()) {
                throw BusinessException.of("File not found");
            }
            
            // Update download count
            fileInfo.setDownloadCount(fileInfo.getDownloadCount() + 1);
            fileInfoRepository.save(fileInfo);
            
            // Set response headers
            response.setContentType(fileInfo.getMimeType());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileInfo.getOriginalName() + "\"");
            response.setContentLengthLong(fileInfo.getFileSize());
            
            // Write file content
            Files.copy(file.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
            
            log.info("File downloaded successfully: {}", fileInfo.getOriginalName());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("File download failed: ", e);
            throw BusinessException.of("File download failed");
        }
    }
    
    @Override
    public void previewFile(Long fileId, HttpServletResponse response) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to preview this file");
            }
            
            // Check if file exists
            File file = new File(fileInfo.getFilePath());
            if (!file.exists()) {
                throw BusinessException.of("File not found");
            }
            
            // Update view count
            fileInfo.setViewCount(fileInfo.getViewCount() + 1);
            fileInfoRepository.save(fileInfo);
            
            // Set response headers
            response.setContentType(fileInfo.getMimeType());
            response.setContentLengthLong(fileInfo.getFileSize());
            
            // Write file content
            Files.copy(file.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
            
            log.info("File previewed successfully: {}", fileInfo.getOriginalName());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("File preview failed: ", e);
            throw BusinessException.of("File preview failed");
        }
    }
    
    @Override
    public void getThumbnail(Long fileId, HttpServletResponse response) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to access this file");
            }
            
            // Check if thumbnail exists
            if (fileInfo.getThumbnailPath() == null) {
                throw BusinessException.of("Thumbnail not found");
            }
            
            File thumbnailFile = new File(fileInfo.getThumbnailPath());
            if (!thumbnailFile.exists()) {
                throw BusinessException.of("Thumbnail not found");
            }
            
            // Set response headers
            response.setContentType("image/jpeg");
            response.setContentLengthLong(thumbnailFile.length());
            
            // Write thumbnail content
            Files.copy(thumbnailFile.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
            
            log.info("Thumbnail retrieved successfully: {}", fileInfo.getOriginalName());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Thumbnail retrieval failed: ", e);
            throw BusinessException.of("Thumbnail retrieval failed");
        }
    }
    
    @Override
    public ApiResponse<FileVO> getFileById(Long fileId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to view this file");
            }
            
            return ApiResponse.success(convertToVO(fileInfo));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get file info: ", e);
            throw BusinessException.of("Failed to get file info");
        }
    }
    
    @Override
    public ApiResponse<FileVO> getFileByFileName(String fileName) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findByTenantIdAndFileName(Long.valueOf(tenantId), fileName)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            return ApiResponse.success(convertToVO(fileInfo));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get file info: ", e);
            throw BusinessException.of("Failed to get file info");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getAllFiles() {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find all files
            List<FileInfo> files = fileInfoRepository.findByTenantId(Long.valueOf(tenantId));
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("Failed to get file list: ", e);
            throw BusinessException.of("Failed to get file list");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> getFilesByPage(Integer page, Integer size) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Paginate files
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<FileInfo> filePage = fileInfoRepository.findByTenantId(Long.valueOf(tenantId), pageable);
            Page<FileVO> fileVOPage = filePage.map(this::convertToVO);
            
            return ApiResponse.success(fileVOPage);
            
        } catch (Exception e) {
            log.error("Failed to paginate files: ", e);
            throw BusinessException.of("Failed to paginate files");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getFilesByType(String fileType) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find files by file type
            List<FileInfo> files = fileInfoRepository.findByTenantIdAndFileType(Long.valueOf(tenantId), fileType);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("Failed to get files by type: ", e);
            throw BusinessException.of("Failed to get files by type");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> getFilesByType(String fileType, Integer page, Integer size) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Paginate files by file type
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<FileInfo> filePage = fileInfoRepository.findByTenantIdAndFileType(Long.valueOf(tenantId), fileType, pageable);
            Page<FileVO> fileVOPage = filePage.map(this::convertToVO);
            
            return ApiResponse.success(fileVOPage);
            
        } catch (Exception e) {
            log.error("Failed to paginate files by type: ", e);
            throw BusinessException.of("Failed to paginate files by type");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getFilesByCategory(String category) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find files by file type category
            List<FileInfo> files = fileInfoRepository.findByTenantId(Long.valueOf(tenantId));
            List<FileVO> fileVOs = files.stream()
                    .filter(file -> FileUtil.getFileTypeCategory(file.getOriginalName()).equals(category))
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("Failed to get files by category: ", e);
            throw BusinessException.of("Failed to get files by category");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> getFilesByCategory(String category, Integer page, Integer size) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Paginate files by file type category
            List<FileInfo> files = fileInfoRepository.findByTenantId(Long.valueOf(tenantId));
            List<FileInfo> filteredFiles = files.stream()
                    .filter(file -> FileUtil.getFileTypeCategory(file.getOriginalName()).equals(category))
                    .collect(Collectors.toList());
            
            // Manual pagination
            int start = page * size;
            int end = Math.min(start + size, filteredFiles.size());
            List<FileInfo> pagedFiles = filteredFiles.subList(start, end);
            
            List<FileVO> fileVOs = pagedFiles.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            // Create pagination object
            Page<FileVO> fileVOPage = new org.springframework.data.domain.PageImpl<>(
                    fileVOs, 
                    PageRequest.of(page, size), 
                    filteredFiles.size()
            );
            
            return ApiResponse.success(fileVOPage);
            
        } catch (Exception e) {
            log.error("Failed to paginate files by category: ", e);
            throw BusinessException.of("Failed to paginate files by category");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getFilesByUploader(Long uploaderId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find files by uploader ID
            List<FileInfo> files = fileInfoRepository.findByTenantIdAndUploaderId(Long.valueOf(tenantId), uploaderId);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("Failed to get files by uploader: ", e);
            throw BusinessException.of("Failed to get files by uploader");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> getFilesByUploader(Long uploaderId, Integer page, Integer size) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Paginate files by uploader ID
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<FileInfo> filePage = fileInfoRepository.findByTenantIdAndUploaderId(Long.valueOf(tenantId), uploaderId, pageable);
            Page<FileVO> fileVOPage = filePage.map(this::convertToVO);
            
            return ApiResponse.success(fileVOPage);
            
        } catch (Exception e) {
            log.error("Failed to paginate files by uploader: ", e);
            throw BusinessException.of("Failed to paginate files by uploader");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> searchFiles(FileSearchRequest request) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Build pagination parameters
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            
            // Find files based on search criteria
            Page<FileInfo> filePage = null;
            
            if (StringUtils.hasText(request.getFileName())) {
                filePage = fileInfoRepository.findByTenantIdAndFileNameContaining(Long.valueOf(tenantId), request.getFileName(), pageable);
            } else if (StringUtils.hasText(request.getOriginalName())) {
                filePage = fileInfoRepository.findByTenantIdAndOriginalNameContaining(Long.valueOf(tenantId), request.getOriginalName(), pageable);
            } else if (StringUtils.hasText(request.getFileType())) {
                filePage = fileInfoRepository.findByTenantIdAndFileType(Long.valueOf(tenantId), request.getFileType(), pageable);
            } else if (StringUtils.hasText(request.getStatus())) {
                filePage = fileInfoRepository.findByTenantIdAndStatus(Long.valueOf(tenantId), request.getStatus(), pageable);
            } else if (request.getUploaderId() != null) {
                filePage = fileInfoRepository.findByTenantIdAndUploaderId(Long.valueOf(tenantId), request.getUploaderId(), pageable);
            } else if (StringUtils.hasText(request.getDescription())) {
                filePage = fileInfoRepository.findByTenantIdAndDescriptionContaining(Long.valueOf(tenantId), request.getDescription(), pageable);
            } else if (StringUtils.hasText(request.getTags())) {
                filePage = fileInfoRepository.findByTenantIdAndTagsContaining(Long.valueOf(tenantId), request.getTags(), pageable);
            } else if (request.getMinFileSize() != null || request.getMaxFileSize() != null) {
                Long minSize = request.getMinFileSize() != null ? request.getMinFileSize() : 0L;
                Long maxSize = request.getMaxFileSize() != null ? request.getMaxFileSize() : Long.MAX_VALUE;
                filePage = fileInfoRepository.findByTenantIdAndFileSizeBetween(Long.valueOf(tenantId), minSize, maxSize, pageable);
            } else if (request.getStartTime() != null || request.getEndTime() != null) {
                LocalDateTime startTime = request.getStartTime() != null ? request.getStartTime() : LocalDateTime.MIN;
                LocalDateTime endTime = request.getEndTime() != null ? request.getEndTime() : LocalDateTime.MAX;
                filePage = fileInfoRepository.findByTenantIdAndCreatedAtBetween(Long.valueOf(tenantId), startTime, endTime, pageable);
            } else {
                // Default query all files
                filePage = fileInfoRepository.findByTenantId(Long.valueOf(tenantId), pageable);
            }
            
            if (filePage != null) {
                Page<FileVO> fileVOPage = filePage.map(this::convertToVO);
                return ApiResponse.success(fileVOPage);
            } else {
                // Handle manual pagination case
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("Search files failed: ", e);
            throw BusinessException.of("Search files failed");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getRecentFiles(Integer limit) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find recently uploaded files
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<FileInfo> files = fileInfoRepository.findRecentFilesByTenantId(Long.valueOf(tenantId), pageable);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("Failed to get recent files: ", e);
            throw BusinessException.of("Failed to get recent files");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getPopularFiles(Integer limit) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find most popular files
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "downloadCount"));
            List<FileInfo> files = fileInfoRepository.findPopularFilesByTenantId(Long.valueOf(tenantId), pageable);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("Failed to get most popular files: ", e);
            throw BusinessException.of("Failed to get most popular files");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getLargestFiles(Integer limit) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find largest files
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "fileSize"));
            List<FileInfo> files = fileInfoRepository.findLargestFilesByTenantId(Long.valueOf(tenantId), pageable);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("Failed to get largest files: ", e);
            throw BusinessException.of("Failed to get largest files");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<FileVO> updateFile(Long fileId, String description, String tags) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to modify this file");
            }
            
            // Update file info
            if (description != null) {
                fileInfo.setDescription(description);
            }
            if (tags != null) {
                fileInfo.setTags(tags);
            }
            
            // Save file info
            FileInfo updatedFileInfo = fileInfoRepository.save(fileInfo);
            
            log.info("File info updated successfully: {}", updatedFileInfo.getOriginalName());
            return ApiResponse.success("File info updated successfully", convertToVO(updatedFileInfo));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update file info: ", e);
            throw BusinessException.of("Failed to update file info");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteFile(Long fileId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to delete this file");
            }
            
            // Soft delete file info
            fileInfo.setStatus("DELETED");
            fileInfo.setDeletedAt(LocalDateTime.now());
            fileInfoRepository.save(fileInfo);
            
            log.info("File deleted successfully: {}", fileInfo.getOriginalName());
            return ApiResponse.<Void>success("File deleted successfully", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to delete file: ", e);
            throw BusinessException.of("Failed to delete file");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeleteFiles(List<Long> fileIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch delete files
            for (Long fileId : fileIds) {
                try {
                    FileInfo fileInfo = fileInfoRepository.findById(fileId)
                            .orElseThrow(() -> BusinessException.of("File not found: " + fileId));
                    
                    // Validate tenant permission
                    if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                        throw BusinessException.of("No permission to delete file: " + fileId);
                    }
                    
                    // Soft delete file info
                    fileInfo.setStatus("DELETED");
                    fileInfo.setDeletedAt(LocalDateTime.now());
                    fileInfoRepository.save(fileInfo);
                } catch (Exception e) {
                    log.error("Failed to delete single file during batch deletion: {}", fileId, e);
                }
            }
            
            log.info("Batch file deletion successful: {}", fileIds);
            return ApiResponse.<Void>success("Batch file deletion successful", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch file deletion failed: ", e);
            throw BusinessException.of("Batch file deletion failed");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> restoreFile(Long fileId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to restore this file");
            }
            
            // Restore file info
            fileInfo.setStatus("ACTIVE");
            fileInfo.setDeletedAt(null);
            fileInfoRepository.save(fileInfo);
            
            log.info("File restored successfully: {}", fileInfo.getOriginalName());
            return ApiResponse.<Void>success("File restored successfully", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to restore file: ", e);
            throw BusinessException.of("Failed to restore file");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchRestoreFiles(List<Long> fileIds) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Batch restore files
            for (Long fileId : fileIds) {
                try {
                    FileInfo fileInfo = fileInfoRepository.findById(fileId)
                            .orElseThrow(() -> BusinessException.of("File not found: " + fileId));
                    
                    // Validate tenant permission
                    if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                        throw BusinessException.of("No permission to restore file: " + fileId);
                    }
                    
                    // Restore file info
                    fileInfo.setStatus("ACTIVE");
                    fileInfo.setDeletedAt(null);
                    fileInfoRepository.save(fileInfo);
                } catch (Exception e) {
                    log.error("Failed to restore single file during batch restoration: {}", fileId, e);
                }
            }
            
            log.info("Batch file restoration successful: {}", fileIds);
            return ApiResponse.<Void>success("Batch file restoration successful", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Batch file restoration failed: ", e);
            throw BusinessException.of("Batch file restoration failed");
        }
    }
    
    @Override
    public ApiResponse<FileStatisticsVO> getFileStatistics() {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            Long tenantIdLong = Long.valueOf(tenantId);
            
            // Calculate start of month
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            // Calculate start of today
            LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            // Get statistics
            Long totalFiles = fileInfoRepository.countByTenantId(tenantIdLong);
            Long activeFiles = fileInfoRepository.countByTenantIdAndStatus(tenantIdLong, "ACTIVE");
            Long deletedFiles = fileInfoRepository.countByTenantIdAndStatus(tenantIdLong, "DELETED");
            Long totalFileSize = fileInfoRepository.sumFileSizeByTenantId(tenantIdLong);
            Long filesUploadedThisMonth = fileInfoRepository.countByTenantIdAndCreatedAtBetween(tenantIdLong, startOfMonth, LocalDateTime.now());
            Long filesUploadedToday = fileInfoRepository.countByTenantIdAndCreatedAtBetween(tenantIdLong, startOfToday, LocalDateTime.now());
            
            // Calculate file size uploaded this month
            Long fileSizeUploadedThisMonth = 0L;
            List<FileInfo> filesThisMonth = fileInfoRepository.findByTenantIdAndCreatedAtBetween(tenantIdLong, startOfMonth, LocalDateTime.now());
            for (FileInfo file : filesThisMonth) {
                fileSizeUploadedThisMonth += file.getFileSize();
            }
            
            // Calculate file size uploaded today
            Long fileSizeUploadedToday = 0L;
            List<FileInfo> filesToday = fileInfoRepository.findByTenantIdAndCreatedAtBetween(tenantIdLong, startOfToday, LocalDateTime.now());
            for (FileInfo file : filesToday) {
                fileSizeUploadedToday += file.getFileSize();
            }
            
            // Calculate total downloads and views
            Long totalDownloads = 0L;
            Long totalViews = 0L;
            List<FileInfo> allFiles = fileInfoRepository.findByTenantId(tenantIdLong);
            for (FileInfo file : allFiles) {
                totalDownloads += file.getDownloadCount();
                totalViews += file.getViewCount();
            }
            
            // Calculate average file size
            Long averageFileSize = activeFiles > 0 ? totalFileSize / activeFiles : 0L;
            
            // Build statistics
            FileStatisticsVO statistics = new FileStatisticsVO();
            statistics.setTotalFiles(totalFiles);
            statistics.setActiveFiles(activeFiles);
            statistics.setDeletedFiles(deletedFiles);
            statistics.setTotalFileSize(totalFileSize);
            statistics.setReadableTotalFileSize(FileUtil.getReadableFileSize(totalFileSize));
            statistics.setFilesUploadedThisMonth(filesUploadedThisMonth);
            statistics.setFileSizeUploadedThisMonth(fileSizeUploadedThisMonth);
            statistics.setReadableFileSizeUploadedThisMonth(FileUtil.getReadableFileSize(fileSizeUploadedThisMonth));
            statistics.setFilesUploadedToday(filesUploadedToday);
            statistics.setFileSizeUploadedToday(fileSizeUploadedToday);
            statistics.setReadableFileSizeUploadedToday(FileUtil.getReadableFileSize(fileSizeUploadedToday));
            statistics.setTotalDownloads(totalDownloads);
            statistics.setTotalViews(totalViews);
            statistics.setAverageFileSize(averageFileSize);
            statistics.setReadableAverageFileSize(FileUtil.getReadableFileSize(averageFileSize));
            
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("Failed to get file statistics: ", e);
            throw BusinessException.of("Failed to get file statistics");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkFileExists(String fileName) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = fileInfoRepository.findByTenantIdAndFileName(Long.valueOf(tenantId), fileName).isPresent();
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("Failed to check if file exists: ", e);
            throw BusinessException.of("Failed to check if file exists");
        }
    }
    
    @Override
    public ApiResponse<String> getDownloadUrl(Long fileId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to access this file");
            }
            
            String downloadUrl = "/api/files/" + fileId + "/download";
            return ApiResponse.success(downloadUrl);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get download URL: ", e);
            throw BusinessException.of("Failed to get download URL");
        }
    }
    
    @Override
    public ApiResponse<String> getPreviewUrl(Long fileId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to access this file");
            }
            
            String previewUrl = "/api/files/" + fileId + "/preview";
            return ApiResponse.success(previewUrl);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get preview URL: ", e);
            throw BusinessException.of("Failed to get preview URL");
        }
    }
    
    @Override
    public ApiResponse<String> getThumbnailUrl(Long fileId) {
        try {
            // Get current tenant ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // Find file info
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("File not found"));
            
            // Validate tenant permission
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("No permission to access this file");
            }
            
            if (fileInfo.getThumbnailPath() == null) {
                throw BusinessException.of("Thumbnail not found");
            }
            
            String thumbnailUrl = "/api/files/" + fileId + "/thumbnail";
            return ApiResponse.success(thumbnailUrl);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to get thumbnail URL: ", e);
            throw BusinessException.of("Failed to get thumbnail URL");
        }
    }
    
    /**
     * Validate file
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.of("File cannot be empty");
        }
        
        if (!FileUtil.isAllowedFileType(file.getOriginalFilename(), fileStorageConfig.getAllowedTypes())) {
            throw BusinessException.of("Unsupported file type");
        }
        
        if (!FileUtil.isFileSizeValid(file.getSize(), fileStorageConfig.getMaxSize())) {
            throw BusinessException.of("File size exceeds limit");
        }
    }
    
    /**
     * Convert FileInfo to FileVO
     */
    private FileVO convertToVO(FileInfo fileInfo) {
        FileVO vo = new FileVO();
        vo.setId(fileInfo.getId());
        vo.setTenantId(fileInfo.getTenantId().toString());
        vo.setFileName(fileInfo.getFileName());
        vo.setOriginalName(fileInfo.getOriginalName());
        vo.setFilePath(fileInfo.getFilePath());
        vo.setFileSize(fileInfo.getFileSize());
        vo.setReadableFileSize(FileUtil.getReadableFileSize(fileInfo.getFileSize()));
        vo.setFileType(fileInfo.getFileType());
        vo.setFileTypeCategory(FileUtil.getFileTypeCategory(fileInfo.getOriginalName()));
        vo.setFileExtension(fileInfo.getFileExtension());
        vo.setMimeType(fileInfo.getMimeType());
        vo.setFileHash(fileInfo.getFileHash());
        vo.setThumbnailPath(fileInfo.getThumbnailPath());
        vo.setStatus(fileInfo.getStatus());
        vo.setUploaderId(fileInfo.getUploaderId());
        vo.setUploaderName(fileInfo.getUploaderName());
        vo.setDescription(fileInfo.getDescription());
        vo.setTags(fileInfo.getTags());
        vo.setDownloadCount(fileInfo.getDownloadCount());
        vo.setViewCount(fileInfo.getViewCount());
        vo.setCreatedAt(fileInfo.getCreatedAt());
        vo.setUpdatedAt(fileInfo.getUpdatedAt());
        vo.setDeletedAt(fileInfo.getDeletedAt());
        
        // Set URLs
        vo.setFileUrl("/api/files/" + fileInfo.getId() + "/preview");
        vo.setDownloadUrl("/api/files/" + fileInfo.getId() + "/download");
        vo.setPreviewUrl("/api/files/" + fileInfo.getId() + "/preview");
        if (fileInfo.getThumbnailPath() != null) {
            vo.setThumbnailUrl("/api/files/" + fileInfo.getId() + "/thumbnail");
        }
        
        return vo;
    }
} 