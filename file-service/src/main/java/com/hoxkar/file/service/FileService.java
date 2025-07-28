package com.hoxkar.file.service;

import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.file.dto.FileSearchRequest;
import com.hoxkar.file.vo.FileStatisticsVO;
import com.hoxkar.file.vo.FileVO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文件服务接口
 */
public interface FileService {
    
    /**
     * 上传文件
     */
    ApiResponse<FileVO> uploadFile(MultipartFile file, String description, String tags, Long uploaderId, String uploaderName);
    
    /**
     * 批量上传文件
     */
    ApiResponse<List<FileVO>> uploadFiles(MultipartFile[] files, String description, String tags, Long uploaderId, String uploaderName);
    
    /**
     * 下载文件
     */
    void downloadFile(Long fileId, HttpServletResponse response);
    
    /**
     * 预览文件
     */
    void previewFile(Long fileId, HttpServletResponse response);
    
    /**
     * 获取缩略图
     */
    void getThumbnail(Long fileId, HttpServletResponse response);
    
    /**
     * 根据ID获取文件信息
     */
    ApiResponse<FileVO> getFileById(Long fileId);
    
    /**
     * 根据文件名获取文件信息
     */
    ApiResponse<FileVO> getFileByFileName(String fileName);
    
    /**
     * 获取所有文件
     */
    ApiResponse<List<FileVO>> getAllFiles();
    
    /**
     * 分页获取文件
     */
    ApiResponse<Page<FileVO>> getFilesByPage(Integer page, Integer size);
    
    /**
     * 根据文件类型获取文件
     */
    ApiResponse<List<FileVO>> getFilesByType(String fileType);
    
    /**
     * 根据文件类型分页获取文件
     */
    ApiResponse<Page<FileVO>> getFilesByType(String fileType, Integer page, Integer size);
    
    /**
     * 根据文件类型分类获取文件
     */
    ApiResponse<List<FileVO>> getFilesByCategory(String category);
    
    /**
     * 根据文件类型分类分页获取文件
     */
    ApiResponse<Page<FileVO>> getFilesByCategory(String category, Integer page, Integer size);
    
    /**
     * 根据上传者ID获取文件
     */
    ApiResponse<List<FileVO>> getFilesByUploader(Long uploaderId);
    
    /**
     * 根据上传者ID分页获取文件
     */
    ApiResponse<Page<FileVO>> getFilesByUploader(Long uploaderId, Integer page, Integer size);
    
    /**
     * 搜索文件
     */
    ApiResponse<Page<FileVO>> searchFiles(FileSearchRequest request);
    
    /**
     * 获取最近上传的文件
     */
    ApiResponse<List<FileVO>> getRecentFiles(Integer limit);
    
    /**
     * 获取最受欢迎的文件
     */
    ApiResponse<List<FileVO>> getPopularFiles(Integer limit);
    
    /**
     * 获取最大的文件
     */
    ApiResponse<List<FileVO>> getLargestFiles(Integer limit);
    
    /**
     * 更新文件信息
     */
    ApiResponse<FileVO> updateFile(Long fileId, String description, String tags);
    
    /**
     * 删除文件
     */
    ApiResponse<Void> deleteFile(Long fileId);
    
    /**
     * 批量删除文件
     */
    ApiResponse<Void> batchDeleteFiles(List<Long> fileIds);
    
    /**
     * 恢复文件
     */
    ApiResponse<Void> restoreFile(Long fileId);
    
    /**
     * 批量恢复文件
     */
    ApiResponse<Void> batchRestoreFiles(List<Long> fileIds);
    
    /**
     * 获取文件统计信息
     */
    ApiResponse<FileStatisticsVO> getFileStatistics();
    
    /**
     * 检查文件是否存在
     */
    ApiResponse<Boolean> checkFileExists(String fileName);
    
    /**
     * 获取文件下载链接
     */
    ApiResponse<String> getDownloadUrl(Long fileId);
    
    /**
     * 获取文件预览链接
     */
    ApiResponse<String> getPreviewUrl(Long fileId);
    
    /**
     * 获取缩略图链接
     */
    ApiResponse<String> getThumbnailUrl(Long fileId);
} 