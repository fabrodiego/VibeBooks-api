package com.vibebooks.api.dto;

import com.vibebooks.api.model.Book;
import com.vibebooks.api.model.BookSentiment;
import com.vibebooks.api.model.ReadingStatus;

import java.util.List;
import java.util.UUID;


/**
 * Represents a book item in the user feed with personalized details.
 */
public record BookFeedDTO(
        UUID id,
        String title,
        String author,
        Integer publicationYear,
        String coverImageUrl,
        long likesCount,
        boolean likedByCurrentUser,
        ReadingStatus status,
        BookSentiment sentiment,
        List<CommentDetailsDTO> comments
) {
    /**
     * Builds a BookFeedDTO from a Book entity and additional data.
     *
     * @param book Book entity
     * @param comments Comments related to the book
     * @param likesCount Total number of likes
     * @param likedByCurrentUser Whether the user liked the book
     * @param status User reading status
     * @param sentiment User sentiment for the book
     */
    public BookFeedDTO(Book book, List<CommentDetailsDTO> comments, long likesCount, boolean likedByCurrentUser,
                       ReadingStatus status, BookSentiment sentiment) {
        this(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublicationYear(),
                book.getCoverImageUrl(),
                likesCount,
                likedByCurrentUser,
                status,
                sentiment,
                comments
        );
    }
}