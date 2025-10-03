package com.vibebooks.api.dto;

import com.vibebooks.api.model.User;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String username,
        String email,
        String bio
) {
    public UserResponseDTO(User user) {
        this(user.getId(), user.getUsername(), user.getEmail(), user.getBio());
    }
}
