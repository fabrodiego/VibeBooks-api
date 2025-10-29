package com.vibebooks.api.dto;

import com.vibebooks.api.model.Book;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a single item in the book feed,
 * containing the book's details and a list of its comments.
 */
public record BookFeedDTO(
        UUID id,
        String title,
        String author,
        Integer publicationYear,
        String coverImageUrl,
        long likesCount,
        boolean likedByCurrentUser,
        List<CommentDetailsDTO> comments
) {
    public BookFeedDTO(Book book, List<CommentDetailsDTO> comments, long likesCount, boolean likedByCurrentUser) {
        this(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublicationYear(),
                book.getCoverImageUrl(),
                likesCount,
                likedByCurrentUser,
                comments
        );
    }
}