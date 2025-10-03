package com.vibebooks.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @Size(min = 3, message = "Username must have at least 3 characters.")
        String username,

        @Email(message = "Invalid email format.")
        String email,

        @Size(max = 255, message = "Bio cannot exceed 255 characters.")
        String bio
) {
}