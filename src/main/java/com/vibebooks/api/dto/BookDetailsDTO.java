package com.vibebooks.api.dto;

import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.BookSentiment;
import com.vibebooks.api.model.ReadingStatus;
import java.util.Map;
import java.util.UUID;

/**
 * Represents detailed book information including user-specific data.
 */
public record BookDetailsDTO(
        UUID id,
        String title,
        String author,
        String isbn,
        Integer publicationYear,
        String coverImageUrl,
        long likesCount,
        boolean likedByCurrentUser,
        ReadingStatus status,
        BookSentiment sentiment,
        Map<BookSentiment, Long> sentimentCounts
) {
    /**
     * Builds a detailed book DTO from a {@link Book} entity.
     *
     * @param book Book entity
     * @param likesCount Total number of likes
     * @param likedByCurrentUser Whether the user liked the book
     * @param status User reading status
     * @param sentiment User sentiment about the book
     */
    public BookDetailsDTO(Book book, long likesCount, boolean likedByCurrentUser, ReadingStatus status, BookSentiment sentiment, Map<BookSentiment, Long> sentimentCounts) {
        this(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getCoverImageUrl(),
                likesCount,
                likedByCurrentUser,
                status,
                sentiment,
                sentimentCounts
        );
    }
}
