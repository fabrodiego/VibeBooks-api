package com.vibebooks.api.dto;

import com.vibebooks.api.model.Comment;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO to display the details of a comment
 */
public record CommentDetailsDTO(
        UUID id,
        String text,
        UUID userId,
        String username,
        OffsetDateTime creationDate,
        UUID bookId,
        String bookTitle,
        long likesCount,
        boolean likedByCurrentUser
) {
    /**
     * @param comment the Comment Entity to be converted
     */
    public CommentDetailsDTO(Comment comment, long likesCount, boolean likedByCurrentUser) {
        this(
                comment.getId(),
                comment.getText(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getCreatedAt(),
                comment.getBook().getId(),
                comment.getBook().getTitle(),
                likesCount,
                likedByCurrentUser
        );
    }
}
