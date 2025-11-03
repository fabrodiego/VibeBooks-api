package com.vibebooks.api.dto;

import com.vibebooks.api.model.ReadingStatus;
import com.vibebooks.api.model.BookSentiment;

/**
 * Request body for updating a user's reading status or sentiment for a book.
 * Example:
 * {
 *   "status": "READING",
 *   "sentiment": "INSPIRING"
 * }
 */
public record BookStatusUpdateDTO(
        ReadingStatus status,
        BookSentiment sentiment
) {
    /**
     * Validates that at least one field (status or sentiment) is provided.
     *
     * @throws IllegalArgumentException if both fields are null
     */
    public BookStatusUpdateDTO {
        if (status == null && sentiment == null) {
            throw new IllegalArgumentException("At least one field (status or sentiment) must be provided.");
        }
    }
}