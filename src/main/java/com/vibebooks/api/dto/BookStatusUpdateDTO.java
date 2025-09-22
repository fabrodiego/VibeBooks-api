package com.vibebooks.api.dto;

import com.vibebooks.api.model.ReadingStatus;
import com.vibebooks.api.model.BookSentiment;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for receiving updates to a user's reading status and sentiment for a book.
 * At least one of the fields must be provided.
 */
public record BookStatusUpdateDTO(
        ReadingStatus status,
        BookSentiment sentiment
) {
    public BookStatusUpdateDTO {
        if (status == null && sentiment == null) {
            throw new IllegalArgumentException("At least one field (status or sentiment) must be provided.");
        }
    }
}