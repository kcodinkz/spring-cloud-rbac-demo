package com.hoxkar.file.controller;

import com.hoxkar.common.pojo.vo.ApiResponse;
import com.hoxkar.file.dto.FileSearchRequest;
import com.hoxkar.file.service.FileService;
import com.hoxkar.file.vo.FileStatisticsVO;
import com.hoxkar.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;

/**
 * 文件控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    private final FileService fileService;
    
    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public ApiResponse<FileVO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
            @RequestParam(value = "uploaderName", required = false) String uploaderName) {
        
        return fileService.uploadFile(file, description, tags, uploaderId, uploaderName);
    }
    
    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    public ApiResponse<List<FileVO>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "uploaderId", required = false) Long uploaderId,
            @RequestParam(value = "uploaderName", required = false) String uploaderName) {
        
        return fileService.uploadFiles(files, description, tags, uploaderId, uploaderName);
    }
    
    /**
     * 下载文件
     */
    @GetMapping("/{fileId}/download")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
        fileService.downloadFile(fileId, response);
    }
    
    /**
     * 预览文件
     */
    @GetMapping("/{fileId}/preview")
    public void previewFile(@PathVariable Long fileId, HttpServletResponse response) {
        fileService.previewFile(fileId, response);
    }
    
    /**
     * 获取缩略图
     */
    @GetMapping("/{fileId}/thumbnail")
    public void getThumbnail(@PathVariable Long fileId, HttpServletResponse response) {
        fileService.getThumbnail(fileId, response);
    }
    
    /**
     * 根据ID获取文件信息
     */
    @GetMapping("/{fileId}")
    public ApiResponse<FileVO> getFileById(@PathVariable Long fileId) {
        return fileService.getFileById(fileId);
    }
    
    /**
     * 根据文件名获取文件信息
     */
    @GetMapping("/by-name/{fileName}")
    public ApiResponse<FileVO> getFileByFileName(@PathVariable String fileName) {
        return fileService.getFileByFileName(fileName);
    }
    
    /**
     * 获取所有文件
     */
    @GetMapping
    public ApiResponse<List<FileVO>> getAllFiles() {
        return fileService.getAllFiles();
    }
    
    /**
     * 分页获取文件
     */
    @GetMapping("/page")
    public ApiResponse<Page<FileVO>> getFilesByPage(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return fileService.getFilesByPage(page, size);
    }
    
    /**
     * 根据文件类型获取文件
     */
    @GetMapping("/by-type/{fileType}")
    public ApiResponse<List<FileVO>> getFilesByType(@PathVariable String fileType) {
        return fileService.getFilesByType(fileType);
    }
    
    /**
     * 根据文件类型分页获取文件
     */
    @GetMapping("/by-type/{fileType}/page")
    public ApiResponse<Page<FileVO>> getFilesByType(
            @PathVariable String fileType,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return fileService.getFilesByType(fileType, page, size);
    }
    
    /**
     * 根据文件类型分类获取文件
     */
    @GetMapping("/by-category/{category}")
    public ApiResponse<List<FileVO>> getFilesByCategory(@PathVariable String category) {
        return fileService.getFilesByCategory(category);
    }
    
    /**
     * 根据文件类型分类分页获取文件
     */
    @GetMapping("/by-category/{category}/page")
    public ApiResponse<Page<FileVO>> getFilesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return fileService.getFilesByCategory(category, page, size);
    }
    
    /**
     * 根据上传者ID获取文件
     */
    @GetMapping("/by-uploader/{uploaderId}")
    public ApiResponse<List<FileVO>> getFilesByUploader(@PathVariable Long uploaderId) {
        return fileService.getFilesByUploader(uploaderId);
    }
    
    /**
     * 根据上传者ID分页获取文件
     */
    @GetMapping("/by-uploader/{uploaderId}/page")
    public ApiResponse<Page<FileVO>> getFilesByUploader(
            @PathVariable Long uploaderId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return fileService.getFilesByUploader(uploaderId, page, size);
    }
    
    /**
     * 搜索文件
     */
    @PostMapping("/search")
    public ApiResponse<Page<FileVO>> searchFiles(@Valid @RequestBody FileSearchRequest request) {
        return fileService.searchFiles(request);
    }
    
    /**
     * 获取最近上传的文件
     */
    @GetMapping("/recent")
    public ApiResponse<List<FileVO>> getRecentFiles(
            @RequestParam(defaultValue = "10") Integer limit) {
        return fileService.getRecentFiles(limit);
    }
    
    /**
     * 获取最受欢迎的文件
     */
    @GetMapping("/popular")
    public ApiResponse<List<FileVO>> getPopularFiles(
            @RequestParam(defaultValue = "10") Integer limit) {
        return fileService.getPopularFiles(limit);
    }
    
    /**
     * 获取最大的文件
     */
    @GetMapping("/largest")
    public ApiResponse<List<FileVO>> getLargestFiles(
            @RequestParam(defaultValue = "10") Integer limit) {
        return fileService.getLargestFiles(limit);
    }
    
    /**
     * 更新文件信息
     */
    @PutMapping("/{fileId}")
    public ApiResponse<FileVO> updateFile(
            @PathVariable Long fileId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "tags", required = false) String tags) {
        return fileService.updateFile(fileId, description, tags);
    }
    
    /**
     * 删除文件
     */
    @DeleteMapping("/{fileId}")
    public ApiResponse<Void> deleteFile(@PathVariable Long fileId) {
        return fileService.deleteFile(fileId);
    }
    
    /**
     * 批量删除文件
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteFiles(@RequestBody List<Long> fileIds) {
        return fileService.batchDeleteFiles(fileIds);
    }
    
    /**
     * 恢复文件
     */
    @PutMapping("/{fileId}/restore")
    public ApiResponse<Void> restoreFile(@PathVariable Long fileId) {
        return fileService.restoreFile(fileId);
    }
    
    /**
     * 批量恢复文件
     */
    @PutMapping("/batch/restore")
    public ApiResponse<Void> batchRestoreFiles(@RequestBody List<Long> fileIds) {
        return fileService.batchRestoreFiles(fileIds);
    }
    
    /**
     * 获取文件统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<FileStatisticsVO> getFileStatistics() {
        return fileService.getFileStatistics();
    }
    
    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists/{fileName}")
    public ApiResponse<Boolean> checkFileExists(@PathVariable String fileName) {
        return fileService.checkFileExists(fileName);
    }
    
    /**
     * 获取文件下载链接
     */
    @GetMapping("/{fileId}/download-url")
    public ApiResponse<String> getDownloadUrl(@PathVariable Long fileId) {
        return fileService.getDownloadUrl(fileId);
    }
    
    /**
     * 获取文件预览链接
     */
    @GetMapping("/{fileId}/preview-url")
    public ApiResponse<String> getPreviewUrl(@PathVariable Long fileId) {
        return fileService.getPreviewUrl(fileId);
    }
    
    /**
     * 获取缩略图链接
     */
    @GetMapping("/{fileId}/thumbnail-url")
    public ApiResponse<String> getThumbnailUrl(@PathVariable Long fileId) {
        return fileService.getThumbnailUrl(fileId);
    }
} 