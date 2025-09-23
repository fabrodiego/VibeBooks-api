package com.vibebooks.api.dto;

import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * A standard DTO for returning API error responses.
 */
public record ErrorResponseDTO(
        Instant timestamp,
        Integer status,
        String error,
        String message,
        String path
) {
    public ErrorResponseDTO(HttpStatus status, String message, String path) {
        this(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }
}