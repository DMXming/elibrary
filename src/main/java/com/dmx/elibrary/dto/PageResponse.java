package com.dmx.elibrary.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Slim pagination envelope: only the fields a client needs, so the JSON
 * stays stable if the persistence layer changes.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
