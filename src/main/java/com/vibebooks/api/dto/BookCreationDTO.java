package com.vibebooks.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookCreationDTO(
        @NotBlank(message = "The title cannot be blank.")
        String title,

        @NotBlank(message = "The author's name cannot be blank.")
        String author,

        @Size(min = 10, max = 13, message = "The ISBN must be between 10 and 13 characters.")
        String isbn,

        Integer publicationYear,

        String coverImageUrl
) {
}
