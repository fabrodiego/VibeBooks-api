package com.vibebooks.api.dto;

import jakarta.validation.constraints.NotBlank;

public record BookIsbnDTO(
        @NotBlank(message = "ISBN cannot be blank.")
        String isbn
) {}