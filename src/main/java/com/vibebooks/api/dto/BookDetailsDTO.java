package com.vibebooks.api.dto;

import com.vibebooks.api.model.Book;
import java.util.UUID;

public record BookDetailsDTO(
        UUID id,
        String title,
        String author,
        String isbn,
        Integer publicationYear,
        String coverImageUrl,
        long likesCount,
        boolean likedByCurrentUser
) {

    // An additional constructor to make it easier to convert the Book entity to this DTO.
    public BookDetailsDTO(Book book, long likesCount, boolean likedByCurrentUser) {
        this(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getCoverImageUrl(),
                likesCount,
                likedByCurrentUser
        );
    }
}
