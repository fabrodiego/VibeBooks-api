package com.vibebooks.api.dto;

public record AuthenticationDTO(
        String login,
        String password
) { }
