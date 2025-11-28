package com.example.starter_kit_restapi_springboot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> results;
    private int page;
    private int limit;
    private long totalResults;
    private int totalPages;

    public static <U, V> PagedResponse<V> fromPage(Page<U> page, List<V> content) {
        return new PagedResponse<>(
                content,
                page.getNumber() + 1, // Page is 0-indexed, but we show 1-indexed
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}