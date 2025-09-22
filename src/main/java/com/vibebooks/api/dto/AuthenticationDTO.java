package com.vibebooks.api.dto;

public record AuthenticationDTO(
        String email,
        String password
) { }
