package com.hoxkar.common.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 统一分页请求参数
 */
@Data
public class PageRequestDTO {
    @Schema(description = "页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "每页条数不能小于1")
    @Max(value = 100, message = "每页条数不能超过100")
    private Integer size = 10;

    @Schema(description = "排序字段", example = "createdAt")
    private String sort;

    @Schema(description = "排序方向，asc/desc", example = "desc")
    private String order;
}