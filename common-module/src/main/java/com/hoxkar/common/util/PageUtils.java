 package com.hoxkar.common.util;

import com.hoxkar.common.pojo.dto.PageRequestDTO;
import com.hoxkar.common.pojo.vo.PageResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 分页工具类
 */
public class PageUtils {
    /**
     * PageRequestDTO转Pageable
     */
    public static Pageable toPageable(PageRequestDTO dto) {
        int page = (dto.getPage() != null && dto.getPage() > 0) ? dto.getPage() - 1 : 0;
        int size = (dto.getSize() != null && dto.getSize() > 0) ? dto.getSize() : 10;
        if (dto.getSort() != null && !dto.getSort().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(dto.getOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
            return PageRequest.of(page, size, direction, dto.getSort());
        } else {
            return PageRequest.of(page, size);
        }
    }

    /**
     * Page转PageResponseVO
     */
    public static <T> PageResponseVO<T> toPageResponseVO(Page<T> page) {
        PageResponseVO<T> vo = new PageResponseVO<>();
        vo.setTotal(page.getTotalElements());
        vo.setPage(page.getNumber() + 1);
        vo.setSize(page.getSize());
        vo.setPages(page.getTotalPages());
        vo.setRecords(page.getContent());
        return vo;
    }

    /**
     * List转PageResponseVO（无分页，仅包装）
     */
    public static <T> PageResponseVO<T> toPageResponseVO(List<T> list) {
        PageResponseVO<T> vo = new PageResponseVO<>();
        vo.setTotal(list.size());
        vo.setPage(1);
        vo.setSize(list.size());
        vo.setPages(1);
        vo.setRecords(list);
        return vo;
    }
}