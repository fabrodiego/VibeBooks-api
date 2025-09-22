package com.vibebooks.api.dto;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String username,
        String email
) {}
