package com.vibebooks.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for receiving the data to create a new comment.
 */
public record CommentCreationDTO(
    @NotBlank(message = "The comment cannot be blank.")
    @Size(max = 1000, message = "The comment cannot exceed 1000 characters.")
    String text
) {
}
