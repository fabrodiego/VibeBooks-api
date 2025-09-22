package com.vibebooks.api.dto;

/**
 * DTO for the response of liking/unliking a book.
 */
public record BookLikeResponseDTO(boolean liked, long totalLikes) {}
