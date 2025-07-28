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
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 验证文件
            validateFile(file);
            
            // 生成文件路径
            String filePath = FileUtil.generateFilePath(fileStorageConfig.getRootPath(), file.getOriginalFilename());
            
            // 创建目录
            FileUtil.createDirectories(FilenameUtils.getFullPath(filePath));
            
            // 保存文件
            File savedFile = new File(filePath);
            file.transferTo(savedFile);
            
            // 计算文件哈希值
            String fileHash = FileUtil.calculateFileHash(file);
            
            // 检查文件是否已存在
            Optional<FileInfo> existingFile = fileInfoRepository.findByTenantIdAndFileHash(Long.valueOf(tenantId), fileHash);
            if (existingFile.isPresent()) {
                // 删除刚保存的文件
                FileUtil.deleteFile(filePath);
                throw BusinessException.of("文件已存在");
            }
            
            // 生成缩略图（如果是图片）
            String thumbnailPath = null;
            if (FileUtil.isImageFile(file.getOriginalFilename())) {
                thumbnailPath = FileUtil.generateThumbnailPath(fileStorageConfig.getThumbnailPath(), filePath);
                FileUtil.createDirectories(FilenameUtils.getFullPath(thumbnailPath));
                FileUtil.generateThumbnail(filePath, thumbnailPath, 
                    fileStorageConfig.getThumbnail().getWidth(), 
                    fileStorageConfig.getThumbnail().getHeight(), 
                    fileStorageConfig.getThumbnail().getQuality());
            }
            
            // 创建文件信息
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
            
            // 保存文件信息
            FileInfo savedFileInfo = fileInfoRepository.save(fileInfo);
            
            log.info("文件上传成功: {}", savedFileInfo.getOriginalName());
            return ApiResponse.success("文件上传成功", convertToVO(savedFileInfo));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败: ", e);
            throw BusinessException.of("文件上传失败");
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
                    log.error("批量上传文件时单个文件上传失败: {}", file.getOriginalFilename(), e);
                }
            }
            
            return ApiResponse.success("批量上传完成", uploadedFiles);
            
        } catch (Exception e) {
            log.error("批量上传文件失败: ", e);
            throw BusinessException.of("批量上传文件失败");
        }
    }
    
    @Override
    public void downloadFile(Long fileId, HttpServletResponse response) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限下载此文件");
            }
            
            // 检查文件是否存在
            File file = new File(fileInfo.getFilePath());
            if (!file.exists()) {
                throw BusinessException.of("文件不存在");
            }
            
            // 更新下载次数
            fileInfo.setDownloadCount(fileInfo.getDownloadCount() + 1);
            fileInfoRepository.save(fileInfo);
            
            // 设置响应头
            response.setContentType(fileInfo.getMimeType());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileInfo.getOriginalName() + "\"");
            response.setContentLengthLong(fileInfo.getFileSize());
            
            // 写入文件内容
            Files.copy(file.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
            
            log.info("文件下载成功: {}", fileInfo.getOriginalName());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件下载失败: ", e);
            throw BusinessException.of("文件下载失败");
        }
    }
    
    @Override
    public void previewFile(Long fileId, HttpServletResponse response) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限预览此文件");
            }
            
            // 检查文件是否存在
            File file = new File(fileInfo.getFilePath());
            if (!file.exists()) {
                throw BusinessException.of("文件不存在");
            }
            
            // 更新查看次数
            fileInfo.setViewCount(fileInfo.getViewCount() + 1);
            fileInfoRepository.save(fileInfo);
            
            // 设置响应头
            response.setContentType(fileInfo.getMimeType());
            response.setContentLengthLong(fileInfo.getFileSize());
            
            // 写入文件内容
            Files.copy(file.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
            
            log.info("文件预览成功: {}", fileInfo.getOriginalName());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件预览失败: ", e);
            throw BusinessException.of("文件预览失败");
        }
    }
    
    @Override
    public void getThumbnail(Long fileId, HttpServletResponse response) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限访问此文件");
            }
            
            // 检查缩略图是否存在
            if (fileInfo.getThumbnailPath() == null) {
                throw BusinessException.of("缩略图不存在");
            }
            
            File thumbnailFile = new File(fileInfo.getThumbnailPath());
            if (!thumbnailFile.exists()) {
                throw BusinessException.of("缩略图不存在");
            }
            
            // 设置响应头
            response.setContentType("image/jpeg");
            response.setContentLengthLong(thumbnailFile.length());
            
            // 写入缩略图内容
            Files.copy(thumbnailFile.toPath(), response.getOutputStream());
            response.getOutputStream().flush();
            
            log.info("缩略图获取成功: {}", fileInfo.getOriginalName());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("缩略图获取失败: ", e);
            throw BusinessException.of("缩略图获取失败");
        }
    }
    
    @Override
    public ApiResponse<FileVO> getFileById(Long fileId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限查看此文件");
            }
            
            return ApiResponse.success(convertToVO(fileInfo));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取文件信息失败: ", e);
            throw BusinessException.of("获取文件信息失败");
        }
    }
    
    @Override
    public ApiResponse<FileVO> getFileByFileName(String fileName) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findByTenantIdAndFileName(Long.valueOf(tenantId), fileName)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            return ApiResponse.success(convertToVO(fileInfo));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取文件信息失败: ", e);
            throw BusinessException.of("获取文件信息失败");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getAllFiles() {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找所有文件
            List<FileInfo> files = fileInfoRepository.findByTenantId(Long.valueOf(tenantId));
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("获取文件列表失败: ", e);
            throw BusinessException.of("获取文件列表失败");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> getFilesByPage(Integer page, Integer size) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 分页查找文件
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<FileInfo> filePage = fileInfoRepository.findByTenantId(Long.valueOf(tenantId), pageable);
            Page<FileVO> fileVOPage = filePage.map(this::convertToVO);
            
            return ApiResponse.success(fileVOPage);
            
        } catch (Exception e) {
            log.error("分页获取文件失败: ", e);
            throw BusinessException.of("分页获取文件失败");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getFilesByType(String fileType) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据文件类型查找文件
            List<FileInfo> files = fileInfoRepository.findByTenantIdAndFileType(Long.valueOf(tenantId), fileType);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("根据文件类型获取文件失败: ", e);
            throw BusinessException.of("根据文件类型获取文件失败");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> getFilesByType(String fileType, Integer page, Integer size) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据文件类型分页查找文件
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<FileInfo> filePage = fileInfoRepository.findByTenantIdAndFileType(Long.valueOf(tenantId), fileType, pageable);
            Page<FileVO> fileVOPage = filePage.map(this::convertToVO);
            
            return ApiResponse.success(fileVOPage);
            
        } catch (Exception e) {
            log.error("根据文件类型分页获取文件失败: ", e);
            throw BusinessException.of("根据文件类型分页获取文件失败");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getFilesByCategory(String category) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据文件类型分类查找文件
            List<FileInfo> files = fileInfoRepository.findByTenantId(Long.valueOf(tenantId));
            List<FileVO> fileVOs = files.stream()
                    .filter(file -> FileUtil.getFileTypeCategory(file.getOriginalName()).equals(category))
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("根据文件类型分类获取文件失败: ", e);
            throw BusinessException.of("根据文件类型分类获取文件失败");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> getFilesByCategory(String category, Integer page, Integer size) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据文件类型分类分页查找文件
            List<FileInfo> files = fileInfoRepository.findByTenantId(Long.valueOf(tenantId));
            List<FileInfo> filteredFiles = files.stream()
                    .filter(file -> FileUtil.getFileTypeCategory(file.getOriginalName()).equals(category))
                    .collect(Collectors.toList());
            
            // 手动分页
            int start = page * size;
            int end = Math.min(start + size, filteredFiles.size());
            List<FileInfo> pagedFiles = filteredFiles.subList(start, end);
            
            List<FileVO> fileVOs = pagedFiles.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            // 创建分页对象
            Page<FileVO> fileVOPage = new org.springframework.data.domain.PageImpl<>(
                    fileVOs, 
                    PageRequest.of(page, size), 
                    filteredFiles.size()
            );
            
            return ApiResponse.success(fileVOPage);
            
        } catch (Exception e) {
            log.error("根据文件类型分类分页获取文件失败: ", e);
            throw BusinessException.of("根据文件类型分类分页获取文件失败");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getFilesByUploader(Long uploaderId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据上传者ID查找文件
            List<FileInfo> files = fileInfoRepository.findByTenantIdAndUploaderId(Long.valueOf(tenantId), uploaderId);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("根据上传者获取文件失败: ", e);
            throw BusinessException.of("根据上传者获取文件失败");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> getFilesByUploader(Long uploaderId, Integer page, Integer size) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 根据上传者ID分页查找文件
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<FileInfo> filePage = fileInfoRepository.findByTenantIdAndUploaderId(Long.valueOf(tenantId), uploaderId, pageable);
            Page<FileVO> fileVOPage = filePage.map(this::convertToVO);
            
            return ApiResponse.success(fileVOPage);
            
        } catch (Exception e) {
            log.error("根据上传者分页获取文件失败: ", e);
            throw BusinessException.of("根据上传者分页获取文件失败");
        }
    }
    
    @Override
    public ApiResponse<Page<FileVO>> searchFiles(FileSearchRequest request) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 构建分页参数
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            
            // 根据搜索条件查找文件
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
                // 默认查询所有文件
                filePage = fileInfoRepository.findByTenantId(Long.valueOf(tenantId), pageable);
            }
            
            if (filePage != null) {
                Page<FileVO> fileVOPage = filePage.map(this::convertToVO);
                return ApiResponse.success(fileVOPage);
            } else {
                // 处理手动分页的情况
                return ApiResponse.success(Page.empty(pageable));
            }
            
        } catch (Exception e) {
            log.error("搜索文件失败: ", e);
            throw BusinessException.of("搜索文件失败");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getRecentFiles(Integer limit) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找最近上传的文件
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
            List<FileInfo> files = fileInfoRepository.findRecentFilesByTenantId(Long.valueOf(tenantId), pageable);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("获取最近文件失败: ", e);
            throw BusinessException.of("获取最近文件失败");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getPopularFiles(Integer limit) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找最受欢迎的文件
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "downloadCount"));
            List<FileInfo> files = fileInfoRepository.findPopularFilesByTenantId(Long.valueOf(tenantId), pageable);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("获取最受欢迎文件失败: ", e);
            throw BusinessException.of("获取最受欢迎文件失败");
        }
    }
    
    @Override
    public ApiResponse<List<FileVO>> getLargestFiles(Integer limit) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找最大的文件
            Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "fileSize"));
            List<FileInfo> files = fileInfoRepository.findLargestFilesByTenantId(Long.valueOf(tenantId), pageable);
            List<FileVO> fileVOs = files.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(fileVOs);
            
        } catch (Exception e) {
            log.error("获取最大文件失败: ", e);
            throw BusinessException.of("获取最大文件失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<FileVO> updateFile(Long fileId, String description, String tags) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限修改此文件");
            }
            
            // 更新文件信息
            if (description != null) {
                fileInfo.setDescription(description);
            }
            if (tags != null) {
                fileInfo.setTags(tags);
            }
            
            // 保存文件信息
            FileInfo updatedFileInfo = fileInfoRepository.save(fileInfo);
            
            log.info("文件信息更新成功: {}", updatedFileInfo.getOriginalName());
            return ApiResponse.success("文件信息更新成功", convertToVO(updatedFileInfo));
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新文件信息失败: ", e);
            throw BusinessException.of("更新文件信息失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> deleteFile(Long fileId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限删除此文件");
            }
            
            // 软删除文件信息
            fileInfo.setStatus("DELETED");
            fileInfo.setDeletedAt(LocalDateTime.now());
            fileInfoRepository.save(fileInfo);
            
            log.info("文件删除成功: {}", fileInfo.getOriginalName());
            return ApiResponse.<Void>success("文件删除成功", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除文件失败: ", e);
            throw BusinessException.of("删除文件失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchDeleteFiles(List<Long> fileIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量删除文件
            for (Long fileId : fileIds) {
                try {
                    FileInfo fileInfo = fileInfoRepository.findById(fileId)
                            .orElseThrow(() -> BusinessException.of("文件不存在: " + fileId));
                    
                    // 验证租户权限
                    if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                        throw BusinessException.of("无权限删除文件: " + fileId);
                    }
                    
                    // 软删除文件信息
                    fileInfo.setStatus("DELETED");
                    fileInfo.setDeletedAt(LocalDateTime.now());
                    fileInfoRepository.save(fileInfo);
                } catch (Exception e) {
                    log.error("批量删除文件时单个文件删除失败: {}", fileId, e);
                }
            }
            
            log.info("批量删除文件成功: {}", fileIds);
            return ApiResponse.<Void>success("批量删除文件成功", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除文件失败: ", e);
            throw BusinessException.of("批量删除文件失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> restoreFile(Long fileId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限恢复此文件");
            }
            
            // 恢复文件信息
            fileInfo.setStatus("ACTIVE");
            fileInfo.setDeletedAt(null);
            fileInfoRepository.save(fileInfo);
            
            log.info("文件恢复成功: {}", fileInfo.getOriginalName());
            return ApiResponse.<Void>success("文件恢复成功", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("恢复文件失败: ", e);
            throw BusinessException.of("恢复文件失败");
        }
    }
    
    @Override
    @Transactional
    public ApiResponse<Void> batchRestoreFiles(List<Long> fileIds) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 批量恢复文件
            for (Long fileId : fileIds) {
                try {
                    FileInfo fileInfo = fileInfoRepository.findById(fileId)
                            .orElseThrow(() -> BusinessException.of("文件不存在: " + fileId));
                    
                    // 验证租户权限
                    if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                        throw BusinessException.of("无权限恢复文件: " + fileId);
                    }
                    
                    // 恢复文件信息
                    fileInfo.setStatus("ACTIVE");
                    fileInfo.setDeletedAt(null);
                    fileInfoRepository.save(fileInfo);
                } catch (Exception e) {
                    log.error("批量恢复文件时单个文件恢复失败: {}", fileId, e);
                }
            }
            
            log.info("批量恢复文件成功: {}", fileIds);
            return ApiResponse.<Void>success("批量恢复文件成功", null);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量恢复文件失败: ", e);
            throw BusinessException.of("批量恢复文件失败");
        }
    }
    
    @Override
    public ApiResponse<FileStatisticsVO> getFileStatistics() {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            Long tenantIdLong = Long.valueOf(tenantId);
            
            // 计算本月开始时间
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            // 计算今日开始时间
            LocalDateTime startOfToday = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            
            // 获取统计信息
            Long totalFiles = fileInfoRepository.countByTenantId(tenantIdLong);
            Long activeFiles = fileInfoRepository.countByTenantIdAndStatus(tenantIdLong, "ACTIVE");
            Long deletedFiles = fileInfoRepository.countByTenantIdAndStatus(tenantIdLong, "DELETED");
            Long totalFileSize = fileInfoRepository.sumFileSizeByTenantId(tenantIdLong);
            Long filesUploadedThisMonth = fileInfoRepository.countByTenantIdAndCreatedAtBetween(tenantIdLong, startOfMonth, LocalDateTime.now());
            Long filesUploadedToday = fileInfoRepository.countByTenantIdAndCreatedAtBetween(tenantIdLong, startOfToday, LocalDateTime.now());
            
            // 计算本月上传文件大小
            Long fileSizeUploadedThisMonth = 0L;
            List<FileInfo> filesThisMonth = fileInfoRepository.findByTenantIdAndCreatedAtBetween(tenantIdLong, startOfMonth, LocalDateTime.now());
            for (FileInfo file : filesThisMonth) {
                fileSizeUploadedThisMonth += file.getFileSize();
            }
            
            // 计算今日上传文件大小
            Long fileSizeUploadedToday = 0L;
            List<FileInfo> filesToday = fileInfoRepository.findByTenantIdAndCreatedAtBetween(tenantIdLong, startOfToday, LocalDateTime.now());
            for (FileInfo file : filesToday) {
                fileSizeUploadedToday += file.getFileSize();
            }
            
            // 计算总下载次数和查看次数
            Long totalDownloads = 0L;
            Long totalViews = 0L;
            List<FileInfo> allFiles = fileInfoRepository.findByTenantId(tenantIdLong);
            for (FileInfo file : allFiles) {
                totalDownloads += file.getDownloadCount();
                totalViews += file.getViewCount();
            }
            
            // 计算平均文件大小
            Long averageFileSize = activeFiles > 0 ? totalFileSize / activeFiles : 0L;
            
            // 构建统计信息
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
            log.error("获取文件统计信息失败: ", e);
            throw BusinessException.of("获取文件统计信息失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkFileExists(String fileName) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            boolean exists = fileInfoRepository.findByTenantIdAndFileName(Long.valueOf(tenantId), fileName).isPresent();
            return ApiResponse.success(exists);
            
        } catch (Exception e) {
            log.error("检查文件是否存在失败: ", e);
            throw BusinessException.of("检查文件是否存在失败");
        }
    }
    
    @Override
    public ApiResponse<String> getDownloadUrl(Long fileId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限访问此文件");
            }
            
            String downloadUrl = "/api/files/" + fileId + "/download";
            return ApiResponse.success(downloadUrl);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取下载链接失败: ", e);
            throw BusinessException.of("获取下载链接失败");
        }
    }
    
    @Override
    public ApiResponse<String> getPreviewUrl(Long fileId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限访问此文件");
            }
            
            String previewUrl = "/api/files/" + fileId + "/preview";
            return ApiResponse.success(previewUrl);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取预览链接失败: ", e);
            throw BusinessException.of("获取预览链接失败");
        }
    }
    
    @Override
    public ApiResponse<String> getThumbnailUrl(Long fileId) {
        try {
            // 获取当前租户ID
            String tenantId = TenantContext.getRequiredTenantId();
            
            // 查找文件信息
            FileInfo fileInfo = fileInfoRepository.findById(fileId)
                    .orElseThrow(() -> BusinessException.of("文件不存在"));
            
            // 验证租户权限
            if (!fileInfo.getTenantId().equals(Long.valueOf(tenantId))) {
                throw BusinessException.of("无权限访问此文件");
            }
            
            if (fileInfo.getThumbnailPath() == null) {
                throw BusinessException.of("缩略图不存在");
            }
            
            String thumbnailUrl = "/api/files/" + fileId + "/thumbnail";
            return ApiResponse.success(thumbnailUrl);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取缩略图链接失败: ", e);
            throw BusinessException.of("获取缩略图链接失败");
        }
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw BusinessException.of("文件不能为空");
        }
        
        if (!FileUtil.isAllowedFileType(file.getOriginalFilename(), fileStorageConfig.getAllowedTypes())) {
            throw BusinessException.of("不支持的文件类型");
        }
        
        if (!FileUtil.isFileSizeValid(file.getSize(), fileStorageConfig.getMaxSize())) {
            throw BusinessException.of("文件大小超过限制");
        }
    }
    
    /**
     * 将FileInfo转换为FileVO
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
        
        // 设置URL
        vo.setFileUrl("/api/files/" + fileInfo.getId() + "/preview");
        vo.setDownloadUrl("/api/files/" + fileInfo.getId() + "/download");
        vo.setPreviewUrl("/api/files/" + fileInfo.getId() + "/preview");
        if (fileInfo.getThumbnailPath() != null) {
            vo.setThumbnailUrl("/api/files/" + fileInfo.getId() + "/thumbnail");
        }
        
        return vo;
    }
} 