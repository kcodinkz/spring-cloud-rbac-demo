package com.hoxkar.common.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 统一分页响应结构
 */
@Data
public class PageResponseVO<T> {
    @Schema(description = "总条数", example = "100")
    private long total;

    @Schema(description = "当前页码", example = "1")
    private int page;

    @Schema(description = "每页条数", example = "10")
    private int size;

    @Schema(description = "总页数", example = "10")
    private int pages;

    @Schema(description = "数据列表")
    private List<T> records;
}