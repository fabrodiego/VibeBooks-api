package com.vibebooks.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for receiving data to update a user's password.
 */
public record UserPasswordUpdateDTO(
        @NotBlank
        String oldPassword,

        @NotBlank
        @Size(min = 6, message = "New password must have at least 6 characters.")
        String newPassword
) {
}