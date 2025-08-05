package com.hoxkar.common.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

// Page Request DTO
/**
 * Unified pagination request parameters // 统一分页请求参数
 */
@Data
public class PageRequestDTO {
    @Schema(description = "Page number, starting from 1", example = "1") // 页码，从1开始
    @Min(value = 1, message = "Page number cannot be less than 1") // 页码不能小于1
    private Integer page = 1;
    @Schema(description = "Number of items per page", example = "10") // 每页条数
    @Min(value = 1, message = "Items per page cannot be less than 1") // 每页条数不能小于1
    @Max(value = 100, message = "Items per page cannot exceed 100") // 每页条数不能超过100
    private Integer size = 10;
    @Schema(description = "Sort field", example = "createdAt") // 排序字段
    private String sort;
    @Schema(description = "Sort direction, asc/desc", example = "desc") // 排序方向，asc/desc
    private String order;
}