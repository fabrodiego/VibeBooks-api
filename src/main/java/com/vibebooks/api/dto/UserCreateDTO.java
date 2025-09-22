package com.vibebooks.api.dto;

public record UserCreateDTO(
   String username,
   String email,
   String rawPassword
) {}
