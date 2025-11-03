package com.vibebooks.api.dto;

import com.vibebooks.api.model.BookSentiment;
import com.vibebooks.api.model.ReadingStatus;

import java.util.UUID;

/**
 * Response body for book status updates.
 * Example:
 * {
 *   "bookId": "a9c4bfa0-3f0a-4d52-a4c5-fb8d8f2c0ef2",
 *   "status": "READING",
 *   "sentiment": "INSPIRING"
 * }
 */
public record BookStatusResponseDTO(
        UUID bookId,
        ReadingStatus status,
        BookSentiment sentiment
) {
    /**
     * Constructs a response containing the user's updated status and sentiment for a book.
     *
     * @param bookId   Book identifier
     * @param status   User's reading status
     * @param sentiment User's sentiment about the book
     */
    public BookStatusResponseDTO {
        // No validation needed; values come from persisted entity
    }
}
