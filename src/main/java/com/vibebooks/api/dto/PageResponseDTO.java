package com.vibebooks.api.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * A standard DTO for returning paginated responses.
 * @param <T> The type of the content in the list.
 */
public record PageResponseDTO<T>(
        List<T> content,
        int currentPage,
        int totalPages,
        long totalElements
) {
    /**
     * Convenience constructor to create a DTO from a Spring Data Page object.
     * @param page The Page object from the repository.
     */
    public PageResponseDTO(Page<T> page) {
        this(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}