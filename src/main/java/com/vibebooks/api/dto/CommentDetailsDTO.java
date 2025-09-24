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
        String username,
        OffsetDateTime creationDate,
        UUID bookId,
        String bookTitle
) {
    /**
     * @param comment the Comment Entity to be converted
     */
    public CommentDetailsDTO(Comment comment) {
        this(
                comment.getId(),
                comment.getText(),
                comment.getUser().getUsername(),
                comment.getCreatedAt(),
                comment.getBook().getId(),
                comment.getBook().getTitle()
        );
    }
}
