package com.vibebooks.api.dto;

import com.vibebooks.api.model.BookSentiment;

public record SentimentCountDTO(
        BookSentiment sentiment,
        long count
) {
}
